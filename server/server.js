const express = require('express');
const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const nodemailer = require('nodemailer');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// Secret for JWT
const JWT_SECRET = "yrc_secret_key_2024";

// MongoDB Connection
const MONGODB_URI = "mongodb+srv://sharan:sharan123@yrc.mekgptr.mongodb.net/?appName=YRC";
mongoose.connect(MONGODB_URI)
    .then(() => console.log("Connected to MongoDB"))
    .catch(err => console.error("Could not connect to MongoDB", err));

// User Schema
const userSchema = new mongoose.Schema({
    fullName: String,
    email: { type: String, unique: true, required: true },
    phoneNumber: String,
    yearJoined: String,
    bloodGroup: String,
    password: { type: String, required: true }
});

const User = mongoose.model('User', userSchema);

// Event Schema
const eventSchema = new mongoose.Schema({
    title: String,
    description: String,
    date: String, // Format: YYYY-MM-DD
    fromTime: String, // Format: HH:mm
    toTime: String, // Format: HH:mm
    location: String,
    postedBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    optedInStudents: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }],
    attendance: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }]
});

const Event = mongoose.model('Event', eventSchema);

// Message Schema
const messageSchema = new mongoose.Schema({
    sender: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    content: { type: String, required: true },
    timestamp: { type: Date, default: Date.now },
    isEdited: { type: Boolean, default: false },
    isDeleted: { type: Boolean, default: false },
    isEmergency: { type: Boolean, default: false }
});

const Message = mongoose.model('Message', messageSchema);

// In-memory OTP storage (Email -> OTP)
const otps = {};

// Email Transporter Configuration
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: 'yrc.otp.mailer@gmail.com',
        pass: 'jekq cwjh nqam pnbj'
    }
});

// Auth Endpoints
app.post('/api/auth/send-otp', async (req, res) => {
    const { email } = req.body;
    if (!email) return res.status(400).json({ success: false, message: "Email is required" });
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    otps[email] = otp;
    console.log(`\n=========================================\nOTP for ${email}: ${otp}\n=========================================\n`);
    try {
        await transporter.sendMail({
            from: '"YRC Admin" <yrc.otp.mailer@gmail.com>',
            to: email,
            subject: "YRC Registration OTP",
            text: `Your OTP for YRC registration is: ${otp}.`
        });
        res.json({ success: true, message: "OTP sent to email" });
    } catch (error) {
        res.json({ success: true, message: "OTP generated! Check server console." });
    }
});

app.post('/api/auth/register', async (req, res) => {
    const { fullName, email, phoneNumber, yearJoined, bloodGroup, password, otp } = req.body;
    if (!otps[email] || otps[email] !== otp) return res.status(400).json({ success: false, message: "Invalid OTP" });
    try {
        const hashedPassword = await bcrypt.hash(password, 10);
        const newUser = new User({ fullName, email, phoneNumber, yearJoined, bloodGroup, password: hashedPassword });
        await newUser.save();
        delete otps[email];
        res.json({ success: true, message: "Registration successful" });
    } catch (error) {
        res.status(500).json({ success: false, message: "Server error" });
    }
});

app.post('/api/auth/login', async (req, res) => {
    const { email, password } = req.body;
    try {
        const user = await User.findOne({ email });
        if (!user || !await bcrypt.compare(password, user.password)) return res.status(401).json({ success: false, message: "Invalid credentials" });
        const token = jwt.sign({ id: user._id, email: user.email }, JWT_SECRET, { expiresIn: '24h' });
        res.json({ token, user: { id: user._id, fullName: user.fullName, email: user.email, phoneNumber: user.phoneNumber, yearJoined: user.yearJoined, bloodGroup: user.bloodGroup } });
    } catch (error) {
        res.status(500).json({ success: false, message: "Server error" });
    }
});

// Event Endpoints
app.post('/api/events', async (req, res) => {
    try {
        const event = new Event(req.body);
        await event.save();
        res.json({ success: true, event });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.get('/api/events', async (req, res) => {
    try {
        const events = await Event.find().populate('postedBy', 'fullName');
        res.json(events);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/events/:id/opt-in', async (req, res) => {
    try {
        const { userId } = req.body;
        await Event.findByIdAndUpdate(req.params.id, { $addToSet: { optedInStudents: userId } });
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/events/:id/opt-out', async (req, res) => {
    try {
        const { userId } = req.body;
        await Event.findByIdAndUpdate(req.params.id, { $pull: { optedInStudents: userId } });
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.get('/api/events/:id/students', async (req, res) => {
    try {
        const event = await Event.findById(req.params.id).populate('optedInStudents', 'fullName email');
        res.json(event.optedInStudents);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/events/:id/attendance', async (req, res) => {
    try {
        const { attendanceList } = req.body; // Array of user IDs
        await Event.findByIdAndUpdate(req.params.id, { attendance: attendanceList });
        res.json({ success: true });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.get('/api/events/:id/attendance', async (req, res) => {
    try {
        const event = await Event.findById(req.params.id);
        res.json(event.attendance);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

// Message Endpoints
app.get('/api/messages', async (req, res) => {
    try {
        const messages = await Message.find().populate('sender', 'fullName email');
        res.json(messages);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/messages', async (req, res) => {
    try {
        const { senderId, content, isEmergency } = req.body;
        const message = new Message({
            sender: senderId,
            content,
            isEmergency: isEmergency || false
        });
        await message.save();
        const populatedMessage = await message.populate('sender', 'fullName email');
        res.json(populatedMessage);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/messages/:id/edit', async (req, res) => {
    try {
        const { content } = req.body;
        await Message.findByIdAndUpdate(req.params.id, {
            content,
            isEdited: true
        });
        res.json({ success: true, message: "Message updated" });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/messages/:id/delete', async (req, res) => {
    try {
        await Message.findByIdAndUpdate(req.params.id, {
            isDeleted: true,
            content: "This message was deleted"
        });
        res.json({ success: true, message: "Message deleted" });
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

// User Endpoints
app.get('/api/users/search-blood', async (req, res) => {
    try {
        const { bloodGroup } = req.query;
        const users = await User.find({ bloodGroup }).select('fullName phoneNumber');
        res.json(users);
    } catch (error) {
        res.status(500).json({ success: false, message: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`YRC Server running on port ${PORT}`));

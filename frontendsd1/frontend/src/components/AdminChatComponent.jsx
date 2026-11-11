import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const AdminChatComponent = () => {
    const [messages, setMessages] = useState([]);
    const [replyInput, setReplyInput] = useState('');
    const [selectedUser, setSelectedUser] = useState(null);
    const [stompClient, setStompClient] = useState(null);
    const isConnected = useRef(false);

    useEffect(() => {
        // Only render if role is admin (simple check, or use props)
        const role = localStorage.getItem('role');
        if (role !== 'ADMIN') {
            console.log("[AdminChatComponent] Not an ADMIN, skipping connection.");
            return;
        }

        const socket = new SockJS('http://localhost/chat/ws');
        const client = Stomp.over(socket);
        client.debug = null;

        client.connect({}, (frame) => {
            console.log('[AdminChatComponent] Connected: ' + frame);
            setStompClient(client);
            isConnected.current = true;

            console.log('[AdminChatComponent] Subscribing to /topic/admin');
            client.subscribe('/topic/admin', (message) => {
                console.log('[AdminChatComponent] Message Received:', message.body);
                const receivedMsg = JSON.parse(message.body);
                setMessages(prev => [...prev, receivedMsg]);
            });
        }, (error) => {
            console.error('[AdminChatComponent] WebSocket error:', error);
        });

        return () => {
            if (client && client.connected) {
                client.disconnect();
            }
        };
    }, []);

    const sendReply = (e) => {
        e.preventDefault();
        if (!replyInput.trim() || !selectedUser || !stompClient) return;

        const replyMsg = {
            sender: "Admin",
            content: replyInput,
            recipient: selectedUser,
            timestamp: Date.now()
        };

        stompClient.send("/app/admin-reply", {}, JSON.stringify(replyMsg));

        setMessages(prev => [...prev, { ...replyMsg, isReply: true }]);

        setReplyInput('');
    };

    const role = localStorage.getItem('role');
    if (role !== 'ADMIN') return null;

    return (
        <div style={{
            position: 'fixed',
            bottom: '20px',
            left: '20px', // Different side from User Chat
            zIndex: 1000,
            width: '400px',
            backgroundColor: 'white',
            border: '1px solid #ccc',
            borderRadius: '8px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
            display: 'flex',
            flexDirection: 'column',
            height: '500px'
        }}>
            <div style={{ padding: '10px', background: '#333', color: 'white', borderTopLeftRadius: '8px', borderTopRightRadius: '8px', fontWeight: 'bold' }}>
                Admin Support Panel
            </div>

            <div style={{ flex: 1, overflowY: 'auto', padding: '10px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {messages.length === 0 && <div style={{ color: '#888', textAlign: 'center' }}>No pending inquiries</div>}
                {messages.map((msg, idx) => (
                    <div key={idx}
                        onClick={() => !msg.isReply && setSelectedUser(msg.sender)}
                        style={{
                            padding: '8px',
                            background: msg.isReply ? '#e6f7ff' : (selectedUser === msg.sender ? '#fffbe6' : '#f0f0f0'),
                            border: selectedUser === msg.sender ? '1px solid #faad14' : '1px solid #eee',
                            borderRadius: '4px',
                            cursor: !msg.isReply ? 'pointer' : 'default',
                            alignSelf: msg.isReply ? 'flex-end' : 'flex-start',
                            maxWidth: '90%'
                        }}>
                        <div style={{ fontSize: '11px', fontWeight: 'bold', marginBottom: '2px', color: '#555' }}>
                            {msg.isReply ? `To: ${msg.recipient}` : `From: ${msg.sender}`}
                        </div>
                        <div>{msg.content}</div>
                    </div>
                ))}
            </div>

            <div style={{ padding: '10px', borderTop: '1px solid #eee', background: '#f9f9f9' }}>
                {selectedUser ? (
                    <form onSubmit={sendReply} style={{ display: 'flex', gap: '5px' }}>
                        <div style={{ fontSize: '12px', marginBottom: '5px' }}>Replying to: <b>{selectedUser}</b></div>
                        <input
                            type="text"
                            value={replyInput}
                            onChange={e => setReplyInput(e.target.value)}
                            placeholder="Type a reply..."
                            style={{ flex: 1, padding: '5px' }}
                        />
                        <button type="submit" style={{ cursor: 'pointer', background: '#1890ff', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '4px' }}>Send</button>
                        <button type="button" onClick={() => setSelectedUser(null)} style={{ cursor: 'pointer', background: '#ccc', color: 'black', border: 'none', padding: '5px', borderRadius: '4px' }}>X</button>
                    </form>
                ) : (
                    <div style={{ fontSize: '12px', color: '#888' }}>Select a user message to reply</div>
                )}
            </div>
        </div>
    );
};

export default AdminChatComponent;

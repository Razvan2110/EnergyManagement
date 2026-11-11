import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

const ChatComponent = () => {
    // State for username, intialized empty
    const [username, setUsername] = useState('');
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [stompClient, setStompClient] = useState(null);
    const [isOpen, setIsOpen] = useState(false);

    // Ref to track if we are already connected (prevents double connection in StrictMode)
    const isConnected = useRef(false);
    const messagesEndRef = useRef(null);

    // Load username from localStorage on mount
    useEffect(() => {
        const storedUser = localStorage.getItem('username');
        if (storedUser) {
            setUsername(storedUser);
        }
    }, []);

    const toggleChat = () => setIsOpen(!isOpen);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    useEffect(() => {
        // Only connect if we have a username and aren't already connected
        if (!username || isConnected.current) return;

        // Connect to WebSocket Service
        const socket = new SockJS('http://localhost/chat/ws');
        const client = Stomp.over(socket);
        // Disable debug logs for cleaner console (optional)
        client.debug = null;

        client.connect({}, (frame) => {
            console.log('[ChatComponent] Connected: ' + frame);
            setStompClient(client);
            isConnected.current = true;

            const topic = '/topic/user/' + username;
            console.log('[ChatComponent] Subscribing to:', topic);

            client.subscribe(topic, (message) => {
                console.log('[ChatComponent] Message Received:', message.body);
                const receivedMsg = JSON.parse(message.body);
                setMessages(prev => [...prev, receivedMsg]);
            });
        }, (error) => {
            console.error('[ChatComponent] WebSocket error:', error);
            isConnected.current = false;
        });

        return () => {
            if (client && client.connected) {
                client.disconnect();
            }
            isConnected.current = false;
        };
    }, [username]);

    const sendMessage = (e) => {
        e.preventDefault();
        if (!input.trim() || !stompClient) return;

        const timestamp = Date.now();
        const userMsg = {
            sender: username,
            content: input,
            timestamp: timestamp
        };

        // UI Optimistic Update: Show my message immediately
        setMessages(prev => [...prev, userMsg]);

        // Send to Backend
        stompClient.send("/app/chat", {}, JSON.stringify(userMsg));
        setInput('');
    };

    // If not logged in, do not render the chat widget
    if (!username) return null;

    return (
        <div style={{
            position: 'fixed',
            bottom: '20px',
            right: '20px',
            zIndex: 1000,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'flex-end',
            fontFamily: 'Arial, sans-serif'
        }}>
            {/* Chat Window */}
            {isOpen && (
                <div style={{
                    width: '350px',
                    height: '450px',
                    backgroundColor: 'white',
                    borderRadius: '10px',
                    boxShadow: '0 5px 20px rgba(0,0,0,0.2)',
                    display: 'flex',
                    flexDirection: 'column',
                    marginBottom: '10px',
                    overflow: 'hidden',
                    border: '1px solid #ddd'
                }}>
                    {/* Header */}
                    <div style={{
                        padding: '15px',
                        backgroundColor: '#1890ff',
                        color: 'white',
                        fontWeight: 'bold',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}>
                        <span>Customer Support</span>
                        <button onClick={toggleChat} style={{ background: 'none', border: 'none', color: 'white', cursor: 'pointer', fontSize: '18px' }}>×</button>
                    </div>

                    {/* Messages Area */}
                    <div style={{
                        flex: 1,
                        padding: '15px',
                        overflowY: 'auto',
                        backgroundColor: '#f5f5f5',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '10px'
                    }}>
                        {messages.length === 0 && (
                            <div style={{ textAlign: 'center', color: '#888', marginTop: '20px', fontSize: '14px' }}>
                                Ask a question! <br /> Try: "consumption", "admin", "device"...
                            </div>
                        )}
                        {messages.map((msg, idx) => {
                            const isMe = msg.sender === username;
                            const isSystem = msg.sender === 'System';
                            return (
                                <div key={idx} style={{
                                    alignSelf: isMe ? 'flex-end' : 'flex-start',
                                    backgroundColor: isMe ? '#1890ff' : (isSystem ? '#52c41a' : '#fff'),
                                    color: (isMe || isSystem) ? 'white' : 'black',
                                    padding: '8px 12px',
                                    borderRadius: '12px',
                                    maxWidth: '80%',
                                    wordWrap: 'break-word',
                                    boxShadow: '0 1px 2px rgba(0,0,0,0.1)',
                                    fontSize: '14px'
                                }}>
                                    <div style={{ fontSize: '10px', opacity: 0.8, marginBottom: '2px' }}>{msg.sender}</div>
                                    {msg.content}
                                </div>
                            );
                        })}
                        <div ref={messagesEndRef} />
                    </div>

                    {/* Input Area */}
                    <form onSubmit={sendMessage} style={{
                        padding: '10px',
                        borderTop: '1px solid #ddd',
                        display: 'flex',
                        gap: '10px',
                        backgroundColor: 'white'
                    }}>
                        <input
                            type="text"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="Type a message..."
                            style={{
                                flex: 1,
                                padding: '8px',
                                borderRadius: '4px',
                                border: '1px solid #ccc',
                                outline: 'none'
                            }}
                        />
                        <button type="submit" style={{
                            padding: '8px 15px',
                            backgroundColor: '#1890ff',
                            color: 'white',
                            border: 'none',
                            borderRadius: '4px',
                            cursor: 'pointer',
                            fontWeight: 'bold'
                        }}>
                            Send
                        </button>
                    </form>
                </div>
            )}

            {/* Floating Button */}
            <button
                onClick={toggleChat}
                style={{
                    width: '60px',
                    height: '60px',
                    borderRadius: '50%',
                    backgroundColor: '#1890ff',
                    color: 'white',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    border: 'none',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
                    cursor: 'pointer',
                    fontSize: '24px',
                    transition: 'transform 0.2s'
                }}
                onMouseOver={(e) => e.currentTarget.style.transform = 'scale(1.1)'}
                onMouseOut={(e) => e.currentTarget.style.transform = 'scale(1)'}
            >
                💬
            </button>
        </div>
    );
};

export default ChatComponent;

import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import deviceService from '../services/devicesService';

const NotificationComponent = () => {
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        
        const username = localStorage.getItem('username');
        const userId = localStorage.getItem('userId');
        const role = localStorage.getItem('role');

        if (!username) return;

        let myDeviceIds = new Set();

        const fetchDevices = async () => {
            if (userId) {
                try {
                    const devices = await deviceService.getDevicesByUserId(userId);
                    devices.forEach(d => myDeviceIds.add(String(d.id))); // Ensure string comparison
                } catch (err) {
                    console.error("Failed to fetch devices for notifications", err);
                }
            }
        };

        const socket = new SockJS('http://localhost/chat/ws');
        const client = Stomp.over(socket);
        client.debug = null;

        const connectAndSubscribe = async () => {
            await fetchDevices();

            client.connect({}, (frame) => {
                console.log('Connected: ' + frame);
                client.subscribe('/topic/notifications', (message) => {
                    const msgContent = message.body;

                    // Filter logic: "device {id} exceeded..."
                    // Try to extract ID
                    const match = msgContent.match(/device\s+(\S+)/i); // \S matches non-whitespace (the ID)
                    if (match && match[1]) {
                        const deviceId = match[1];
                        // If I own this device, show it
                        if (myDeviceIds.has(deviceId)) {
                            showNotification(msgContent);
                        }
                    } else {
                        // Fallback: if format is unknown, maybe show it? 
                        // Or safety: don't show.
                        // Given the strict user complaint, let's only show if verified.
                    }
                });
            }, (error) => {
                console.error('WebSocket connection error:', error);
            });
        };

        connectAndSubscribe();

        return () => {
            if (client) {
                try {
                    client.disconnect();
                } catch (e) {
                    console.log("Disconnect error:", e);
                }
            }
        };
    }, []);

    const showNotification = (message) => {
        const id = Date.now() + Math.random().toString(36).substr(2, 9);
        setNotifications(prev => {
            const isDuplicate = prev.some(n => n.message === message && Date.now() - parseInt(n.id.toString().substring(0, 13)) < 1000);
            if (isDuplicate) return prev;
            return [...prev, { id, message }];
        });

        setTimeout(() => {
            removeNotification(id);
        }, 5000);
    };

    const removeNotification = (id) => {
        setNotifications(prev => prev.filter(n => n.id !== id));
    };

    return (
        <div style={{
            position: 'fixed',
            top: '20px',
            right: '20px',
            zIndex: 1000,
            display: 'flex',
            flexDirection: 'column',
            gap: '10px'
        }}>
            {notifications.map(n => (
                <div key={n.id} style={{
                    backgroundColor: '#ff4d4f', // Red for alert
                    color: 'white',
                    padding: '16px 24px',
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    minWidth: '300px',
                    animation: 'slideIn 0.3s ease-out'
                }}>
                    <span>{n.message}</span>
                    <button
                        onClick={() => removeNotification(n.id)}
                        style={{
                            background: 'transparent',
                            border: 'none',
                            color: 'white',
                            marginLeft: '10px',
                            cursor: 'pointer',
                            fontSize: '16px'
                        }}
                    >
                        ×
                    </button>
                </div>
            ))}
            <style>{`
                @keyframes slideIn {
                    from { transform: translateX(100%); opacity: 0; }
                    to { transform: translateX(0); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export default NotificationComponent;

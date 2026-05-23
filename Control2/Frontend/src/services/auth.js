import api from './http-common.js';

const subscribers = [];

let currentUser = null;

function notify() {
	subscribers.forEach((subscriber) => subscriber(currentUser));
}

export function subscribe(callback) {
	subscribers.push(callback);
	return () => {
		const index = subscribers.indexOf(callback);
		if (index >= 0) {
			subscribers.splice(index, 1);
		}
	};
}

export function getUser() {
	return currentUser;
}

export function isAuthenticated() {
	return !!currentUser;
}

function setSession(user) {
	currentUser = user;
	notify();
}

export function clearSession() {
	setSession(null);
}

export async function restoreSession() {
	try {
		const response = await api.get('/api/auth/me');
		setSession({ username: response.data.username });
		return currentUser;
	} catch (error) {
		clearSession();
		return null;
	}
}

export async function login(payload) {
	const response = await api.post('/api/auth/login', payload);
	setSession({ username: response.data.username });
	return response.data;
}

export async function register(payload) {
	const response = await api.post('/api/auth/register', payload);
	setSession({ username: response.data.username });
	return response.data;
}

export async function logout() {
	try {
		await api.post('/api/auth/logout');
	} finally {
		clearSession();
	}
}

export default {
	subscribe,
	getUser,
	isAuthenticated,
	restoreSession,
	login,
	register,
	logout,
	clearSession,
};

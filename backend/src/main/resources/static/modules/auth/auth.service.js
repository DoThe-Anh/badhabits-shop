import { loginRequest } from './auth.api.js';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

function validateCredentials(username, password) {
  if (!username || typeof username !== 'string' || username.trim().length < 3) {
    throw new Error('Username must be at least 3 characters');
  }
  if (!password || typeof password !== 'string' || password.length < 6) {
    throw new Error('Password must be at least 6 characters');
  }
}

export async function login(username, password) {
  validateCredentials(username, password);

  try {
    const response = await loginRequest({
      username: username.trim(),
      password,
    });

    const { token, user } = response.data;
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));

    return user;
  } catch (err) {
    if (err.code === 'AUTH_INVALID_CREDENTIALS') {
      throw new Error('Wrong username or password. Please try again.');
    }
    throw new Error('Unable to sign in right now. Please try again later.');
  }
}

export function logout() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getCurrentUser() {
  const raw = localStorage.getItem(USER_KEY);
  return raw ? JSON.parse(raw) : null;
}

const MOCK_USERS = [
  { username: 'admin', password: '123456', name: 'Administrator' },
  { username: 'user', password: 'password', name: 'Normal User' },
];

const delay = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

export async function loginRequest({ username, password }) {
  await delay(600);

  const user = MOCK_USERS.find((u) => u.username === username);

  if (!user || user.password !== password) {
    const error = new Error('Invalid username or password');
    error.code = 'AUTH_INVALID_CREDENTIALS';
    throw error;
  }

  return {
    success: true,
    data: {
      token: `mock-token-${user.username}-${Date.now()}`,
      user: { username: user.username, name: user.name },
    },
  };
}

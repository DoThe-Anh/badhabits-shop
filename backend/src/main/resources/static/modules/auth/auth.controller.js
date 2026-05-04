import { login } from './auth.service.js';

function setMessage(el, text, type) {
  el.textContent = text;
  el.className = `message message--${type}`;
  el.hidden = !text;
}

function setLoading(form, isLoading) {
  const submitBtn = form.querySelector('button[type="submit"]');
  const inputs = form.querySelectorAll('input');
  submitBtn.disabled = isLoading;
  submitBtn.textContent = isLoading ? 'Signing in...' : 'Sign in';
  inputs.forEach((input) => {
    input.disabled = isLoading;
  });
}

async function handleSubmit(event, form, messageEl) {
  event.preventDefault();

  const formData = new FormData(form);
  const username = formData.get('username');
  const password = formData.get('password');

  setMessage(messageEl, '', 'info');
  setLoading(form, true);

  try {
    const user = await login(username, password);
    setMessage(messageEl, `Welcome, ${user.name}!`, 'success');
    form.reset();
  } catch (err) {
    setMessage(messageEl, err.message, 'error');
  } finally {
    setLoading(form, false);
  }
}

export function initLoginController() {
  const form = document.getElementById('login-form');
  const messageEl = document.getElementById('login-message');

  if (!form || !messageEl) {
    throw new Error('Login form elements not found in DOM');
  }

  form.addEventListener('submit', (event) =>
    handleSubmit(event, form, messageEl),
  );
}

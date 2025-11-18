import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { login } from '../store/authSlice';

function Login() {
  const [credentials, setCredentials] = useState({ username: '', password: '' });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(login(credentials));
    if (result.type === 'auth/login/fulfilled') {
      navigate('/dashboard');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 to-gray-800">
      <div className="bg-gray-800 p-8 rounded-lg shadow-xl w-96">
        <h2 className="text-3xl font-bold text-center mb-6 text-white">Crypto Exchange</h2>
        <h3 className="text-xl text-center mb-6 text-gray-300">Login</h3>

        {error && (
          <div className="bg-red-500 text-white p-3 rounded mb-4">
            {typeof error === 'string' ? error : 'Login failed'}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-gray-300 mb-2">Username</label>
            <input
              type="text"
              className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
              value={credentials.username}
              onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
              required
            />
          </div>

          <div className="mb-6">
            <label className="block text-gray-300 mb-2">Password</label>
            <input
              type="password"
              className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
              value={credentials.password}
              onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded transition duration-200 disabled:opacity-50"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p className="text-center text-gray-400 mt-4">
          Don't have an account?{' '}
          <Link to="/register" className="text-blue-400 hover:text-blue-300">
            Register
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Login;

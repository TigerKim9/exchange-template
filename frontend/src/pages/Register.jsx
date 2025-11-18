import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../store/authSlice';

function Register() {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
  });
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const result = await dispatch(register(formData));
    if (result.type === 'auth/register/fulfilled') {
      navigate('/dashboard');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 to-gray-800 py-12">
      <div className="bg-gray-800 p-8 rounded-lg shadow-xl w-96">
        <h2 className="text-3xl font-bold text-center mb-6 text-white">Crypto Exchange</h2>
        <h3 className="text-xl text-center mb-6 text-gray-300">Register</h3>

        {error && (
          <div className="bg-red-500 text-white p-3 rounded mb-4">
            {typeof error === 'string' ? error : 'Registration failed'}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-gray-300 mb-2">Username</label>
            <input
              type="text"
              className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-gray-300 mb-2">Email</label>
            <input
              type="email"
              className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-gray-300 mb-2">Password</label>
            <input
              type="password"
              className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-gray-300 mb-2">First Name</label>
            <input
              type="text"
              className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
            />
          </div>

          <div className="mb-6">
            <label className="block text-gray-300 mb-2">Last Name</label>
            <input
              type="text"
              className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600 focus:border-blue-500 focus:outline-none"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded transition duration-200 disabled:opacity-50"
          >
            {loading ? 'Registering...' : 'Register'}
          </button>
        </form>

        <p className="text-center text-gray-400 mt-4">
          Already have an account?{' '}
          <Link to="/login" className="text-blue-400 hover:text-blue-300">
            Login
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Register;

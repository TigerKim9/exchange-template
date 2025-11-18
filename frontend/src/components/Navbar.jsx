import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../store/authSlice';

function Navbar() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { user } = useSelector((state) => state.auth);

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  return (
    <nav className="bg-gray-800 border-b border-gray-700">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center space-x-8">
            <Link to="/dashboard" className="text-white text-xl font-bold">
              Crypto Exchange
            </Link>
            <div className="hidden md:flex space-x-4">
              <Link to="/dashboard" className="text-gray-300 hover:text-white px-3 py-2 rounded">
                Dashboard
              </Link>
              <Link to="/trading" className="text-gray-300 hover:text-white px-3 py-2 rounded">
                Trading
              </Link>
              <Link to="/wallet" className="text-gray-300 hover:text-white px-3 py-2 rounded">
                Wallet
              </Link>
              <Link to="/transactions" className="text-gray-300 hover:text-white px-3 py-2 rounded">
                Transactions
              </Link>
              {user?.role === 'ROLE_ADMIN' && (
                <Link to="/admin" className="text-gray-300 hover:text-white px-3 py-2 rounded">
                  Admin
                </Link>
              )}
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-gray-300">Welcome, {user?.username}</span>
            <button
              onClick={handleLogout}
              className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded transition duration-200"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;

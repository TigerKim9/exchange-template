import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import { adminAPI } from '../services/api';

function AdminPanel() {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [statsRes, usersRes] = await Promise.all([
        adminAPI.getStats(),
        adminAPI.getUsers(),
      ]);
      setStats(statsRes.data);
      setUsers(usersRes.data);
    } catch (error) {
      console.error('Failed to load admin data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleUser = async (userId, enabled) => {
    try {
      if (enabled) {
        await adminAPI.disableUser(userId);
      } else {
        await adminAPI.enableUser(userId);
      }
      loadData();
    } catch (error) {
      console.error('Failed to toggle user:', error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-white mb-8">Admin Panel</h1>

        {loading ? (
          <p className="text-gray-400">Loading...</p>
        ) : (
          <>
            {/* Stats */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              <div className="bg-gray-800 p-6 rounded-lg">
                <h3 className="text-gray-400 text-sm mb-2">Total Users</h3>
                <p className="text-3xl font-bold text-white">{stats?.totalUsers || 0}</p>
              </div>
              <div className="bg-gray-800 p-6 rounded-lg">
                <h3 className="text-gray-400 text-sm mb-2">Total Orders</h3>
                <p className="text-3xl font-bold text-white">{stats?.totalOrders || 0}</p>
              </div>
              <div className="bg-gray-800 p-6 rounded-lg">
                <h3 className="text-gray-400 text-sm mb-2">Total Transactions</h3>
                <p className="text-3xl font-bold text-white">
                  {stats?.totalTransactions || 0}
                </p>
              </div>
              <div className="bg-gray-800 p-6 rounded-lg">
                <h3 className="text-gray-400 text-sm mb-2">Trading Pairs</h3>
                <p className="text-3xl font-bold text-white">
                  {stats?.totalTradingPairs || 0}
                </p>
              </div>
            </div>

            {/* Users Table */}
            <div className="bg-gray-800 p-6 rounded-lg">
              <h2 className="text-2xl font-bold text-white mb-4">Users</h2>
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead>
                    <tr className="border-b border-gray-700">
                      <th className="text-left py-3 text-gray-400">ID</th>
                      <th className="text-left py-3 text-gray-400">Username</th>
                      <th className="text-left py-3 text-gray-400">Email</th>
                      <th className="text-left py-3 text-gray-400">Role</th>
                      <th className="text-center py-3 text-gray-400">Status</th>
                      <th className="text-left py-3 text-gray-400">Registered</th>
                      <th className="text-center py-3 text-gray-400">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((user) => (
                      <tr key={user.id} className="border-b border-gray-700">
                        <td className="py-3 text-white">{user.id}</td>
                        <td className="py-3 text-white font-semibold">{user.username}</td>
                        <td className="py-3 text-gray-300">{user.email}</td>
                        <td className="py-3 text-blue-400">{user.role}</td>
                        <td className="py-3 text-center">
                          <span
                            className={`px-2 py-1 rounded text-xs font-semibold ${
                              user.enabled
                                ? 'bg-green-600 text-white'
                                : 'bg-red-600 text-white'
                            }`}
                          >
                            {user.enabled ? 'Active' : 'Disabled'}
                          </span>
                        </td>
                        <td className="py-3 text-gray-400 text-sm">
                          {new Date(user.createdAt).toLocaleDateString()}
                        </td>
                        <td className="py-3 text-center">
                          <button
                            onClick={() => handleToggleUser(user.id, user.enabled)}
                            className={`px-3 py-1 rounded text-sm font-semibold ${
                              user.enabled
                                ? 'bg-red-600 hover:bg-red-700'
                                : 'bg-green-600 hover:bg-green-700'
                            }`}
                          >
                            {user.enabled ? 'Disable' : 'Enable'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

export default AdminPanel;

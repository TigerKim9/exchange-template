import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Navbar from '../components/Navbar';
import { fetchWallets } from '../store/walletSlice';
import { fetchTradingPairs } from '../store/tradingSlice';

function Dashboard() {
  const dispatch = useDispatch();
  const { wallets } = useSelector((state) => state.wallet);
  const { tradingPairs } = useSelector((state) => state.trading);
  const { user } = useSelector((state) => state.auth);

  useEffect(() => {
    dispatch(fetchWallets());
    dispatch(fetchTradingPairs());
  }, [dispatch]);

  return (
    <div className="min-h-screen bg-gray-900">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-white mb-8">
          Welcome back, {user?.username}!
        </h1>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
          <div className="bg-gray-800 p-6 rounded-lg">
            <h3 className="text-gray-400 text-sm mb-2">Total Wallets</h3>
            <p className="text-3xl font-bold text-white">{wallets.length}</p>
          </div>
          <div className="bg-gray-800 p-6 rounded-lg">
            <h3 className="text-gray-400 text-sm mb-2">Active Markets</h3>
            <p className="text-3xl font-bold text-white">{tradingPairs.length}</p>
          </div>
          <div className="bg-gray-800 p-6 rounded-lg">
            <h3 className="text-gray-400 text-sm mb-2">Account Status</h3>
            <p className="text-3xl font-bold text-green-500">Active</p>
          </div>
        </div>

        <div className="bg-gray-800 p-6 rounded-lg mb-8">
          <h2 className="text-2xl font-bold text-white mb-4">Your Wallets</h2>
          {wallets.length === 0 ? (
            <p className="text-gray-400">No wallets found. Create one to get started!</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="text-left py-3 text-gray-400">Currency</th>
                    <th className="text-right py-3 text-gray-400">Balance</th>
                    <th className="text-right py-3 text-gray-400">Available</th>
                    <th className="text-right py-3 text-gray-400">Locked</th>
                  </tr>
                </thead>
                <tbody>
                  {wallets.map((wallet) => (
                    <tr key={wallet.id} className="border-b border-gray-700">
                      <td className="py-3 text-white font-semibold">{wallet.currency}</td>
                      <td className="py-3 text-right text-white">{wallet.balance}</td>
                      <td className="py-3 text-right text-green-500">{wallet.availableBalance}</td>
                      <td className="py-3 text-right text-yellow-500">{wallet.lockedBalance}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="bg-gray-800 p-6 rounded-lg">
          <h2 className="text-2xl font-bold text-white mb-4">Market Overview</h2>
          {tradingPairs.length === 0 ? (
            <p className="text-gray-400">No trading pairs available.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="text-left py-3 text-gray-400">Pair</th>
                    <th className="text-right py-3 text-gray-400">Price</th>
                    <th className="text-right py-3 text-gray-400">24h Change</th>
                    <th className="text-right py-3 text-gray-400">24h Volume</th>
                  </tr>
                </thead>
                <tbody>
                  {tradingPairs.map((pair) => (
                    <tr key={pair.id} className="border-b border-gray-700">
                      <td className="py-3 text-white font-semibold">{pair.symbol}</td>
                      <td className="py-3 text-right text-white">
                        ${pair.currentPrice || '0.00'}
                      </td>
                      <td
                        className={`py-3 text-right ${
                          (pair.priceChangePercent24h || 0) >= 0
                            ? 'text-green-500'
                            : 'text-red-500'
                        }`}
                      >
                        {(pair.priceChangePercent24h || 0).toFixed(2)}%
                      </td>
                      <td className="py-3 text-right text-white">
                        {pair.volume24h || '0.00'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default Dashboard;

import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Navbar from '../components/Navbar';
import { fetchWallets, deposit, withdraw } from '../store/walletSlice';

function Wallet() {
  const dispatch = useDispatch();
  const { wallets } = useSelector((state) => state.wallet);
  const [activeTab, setActiveTab] = useState('deposit');
  const [formData, setFormData] = useState({
    currency: 'BTC',
    amount: '',
    walletAddress: '',
    transactionHash: '',
  });

  useEffect(() => {
    dispatch(fetchWallets());
  }, [dispatch]);

  const handleDeposit = async (e) => {
    e.preventDefault();
    await dispatch(deposit({
      currency: formData.currency,
      amount: parseFloat(formData.amount),
      transactionHash: formData.transactionHash,
    }));
    setFormData({ ...formData, amount: '', transactionHash: '' });
    dispatch(fetchWallets());
  };

  const handleWithdraw = async (e) => {
    e.preventDefault();
    await dispatch(withdraw({
      currency: formData.currency,
      amount: parseFloat(formData.amount),
      walletAddress: formData.walletAddress,
    }));
    setFormData({ ...formData, amount: '', walletAddress: '' });
    dispatch(fetchWallets());
  };

  return (
    <div className="min-h-screen bg-gray-900">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-white mb-8">Wallet</h1>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Wallets List */}
          <div className="bg-gray-800 p-6 rounded-lg">
            <h2 className="text-2xl font-bold text-white mb-4">Your Wallets</h2>
            {wallets.length === 0 ? (
              <p className="text-gray-400">No wallets found</p>
            ) : (
              <div className="space-y-4">
                {wallets.map((wallet) => (
                  <div key={wallet.id} className="bg-gray-700 p-4 rounded">
                    <div className="flex justify-between items-center mb-2">
                      <span className="text-xl font-bold text-white">
                        {wallet.currency}
                      </span>
                      <span className="text-2xl font-bold text-white">
                        {wallet.balance}
                      </span>
                    </div>
                    <div className="grid grid-cols-2 gap-2 text-sm">
                      <div>
                        <span className="text-gray-400">Available:</span>
                        <span className="text-green-500 ml-2">
                          {wallet.availableBalance}
                        </span>
                      </div>
                      <div>
                        <span className="text-gray-400">Locked:</span>
                        <span className="text-yellow-500 ml-2">
                          {wallet.lockedBalance}
                        </span>
                      </div>
                    </div>
                    {wallet.walletAddress && (
                      <div className="mt-2 text-xs text-gray-400">
                        Address: {wallet.walletAddress}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Deposit/Withdraw Form */}
          <div className="bg-gray-800 p-6 rounded-lg">
            <div className="flex space-x-4 mb-6">
              <button
                onClick={() => setActiveTab('deposit')}
                className={`flex-1 py-2 px-4 rounded ${
                  activeTab === 'deposit'
                    ? 'bg-blue-600'
                    : 'bg-gray-700 hover:bg-gray-600'
                }`}
              >
                Deposit
              </button>
              <button
                onClick={() => setActiveTab('withdraw')}
                className={`flex-1 py-2 px-4 rounded ${
                  activeTab === 'withdraw'
                    ? 'bg-blue-600'
                    : 'bg-gray-700 hover:bg-gray-600'
                }`}
              >
                Withdraw
              </button>
            </div>

            {activeTab === 'deposit' ? (
              <form onSubmit={handleDeposit} className="space-y-4">
                <div>
                  <label className="block text-gray-300 mb-2">Currency</label>
                  <select
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={formData.currency}
                    onChange={(e) =>
                      setFormData({ ...formData, currency: e.target.value })
                    }
                  >
                    <option value="BTC">BTC</option>
                    <option value="ETH">ETH</option>
                    <option value="USDT">USDT</option>
                    <option value="USD">USD</option>
                  </select>
                </div>

                <div>
                  <label className="block text-gray-300 mb-2">Amount</label>
                  <input
                    type="number"
                    step="0.00000001"
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={formData.amount}
                    onChange={(e) =>
                      setFormData({ ...formData, amount: e.target.value })
                    }
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-300 mb-2">
                    Transaction Hash (Optional)
                  </label>
                  <input
                    type="text"
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={formData.transactionHash}
                    onChange={(e) =>
                      setFormData({ ...formData, transactionHash: e.target.value })
                    }
                  />
                </div>

                <button
                  type="submit"
                  className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-4 rounded transition"
                >
                  Deposit
                </button>
              </form>
            ) : (
              <form onSubmit={handleWithdraw} className="space-y-4">
                <div>
                  <label className="block text-gray-300 mb-2">Currency</label>
                  <select
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={formData.currency}
                    onChange={(e) =>
                      setFormData({ ...formData, currency: e.target.value })
                    }
                  >
                    <option value="BTC">BTC</option>
                    <option value="ETH">ETH</option>
                    <option value="USDT">USDT</option>
                    <option value="USD">USD</option>
                  </select>
                </div>

                <div>
                  <label className="block text-gray-300 mb-2">Amount</label>
                  <input
                    type="number"
                    step="0.00000001"
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={formData.amount}
                    onChange={(e) =>
                      setFormData({ ...formData, amount: e.target.value })
                    }
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-300 mb-2">
                    Withdrawal Address
                  </label>
                  <input
                    type="text"
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={formData.walletAddress}
                    onChange={(e) =>
                      setFormData({ ...formData, walletAddress: e.target.value })
                    }
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-4 rounded transition"
                >
                  Withdraw
                </button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Wallet;

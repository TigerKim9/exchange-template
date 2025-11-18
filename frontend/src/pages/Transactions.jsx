import { useEffect, useState } from 'react';
import Navbar from '../components/Navbar';
import { transactionAPI } from '../services/api';

function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTransactions();
  }, []);

  const loadTransactions = async () => {
    try {
      const response = await transactionAPI.getTransactions();
      setTransactions(response.data);
    } catch (error) {
      console.error('Failed to load transactions:', error);
    } finally {
      setLoading(false);
    }
  };

  const getTypeColor = (type) => {
    switch (type) {
      case 'DEPOSIT':
      case 'TRADE_BUY':
        return 'text-green-500';
      case 'WITHDRAWAL':
      case 'TRADE_SELL':
        return 'text-red-500';
      case 'FEE':
        return 'text-yellow-500';
      default:
        return 'text-gray-400';
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'COMPLETED':
        return 'text-green-500';
      case 'PENDING':
      case 'PROCESSING':
        return 'text-yellow-500';
      case 'FAILED':
      case 'CANCELLED':
        return 'text-red-500';
      default:
        return 'text-gray-400';
    }
  };

  return (
    <div className="min-h-screen bg-gray-900">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-white mb-8">Transaction History</h1>

        <div className="bg-gray-800 p-6 rounded-lg">
          {loading ? (
            <p className="text-gray-400">Loading transactions...</p>
          ) : transactions.length === 0 ? (
            <p className="text-gray-400">No transactions found</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-gray-700">
                    <th className="text-left py-3 text-gray-400">Date</th>
                    <th className="text-left py-3 text-gray-400">Type</th>
                    <th className="text-left py-3 text-gray-400">Currency</th>
                    <th className="text-right py-3 text-gray-400">Amount</th>
                    <th className="text-right py-3 text-gray-400">Fee</th>
                    <th className="text-center py-3 text-gray-400">Status</th>
                    <th className="text-left py-3 text-gray-400">Description</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx) => (
                    <tr key={tx.id} className="border-b border-gray-700">
                      <td className="py-3 text-gray-300 text-sm">
                        {new Date(tx.createdAt).toLocaleString()}
                      </td>
                      <td className={`py-3 font-semibold ${getTypeColor(tx.type)}`}>
                        {tx.type}
                      </td>
                      <td className="py-3 text-white">{tx.currency}</td>
                      <td className="py-3 text-right text-white">
                        {tx.amount}
                      </td>
                      <td className="py-3 text-right text-gray-400">
                        {tx.fee || '0.00'}
                      </td>
                      <td className={`py-3 text-center font-semibold ${getStatusColor(tx.status)}`}>
                        {tx.status}
                      </td>
                      <td className="py-3 text-gray-400 text-sm">
                        {tx.description}
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

export default Transactions;

import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import Navbar from '../components/Navbar';
import { fetchTradingPairs, fetchUserOrders, createOrder, setSelectedPair } from '../store/tradingSlice';

function Trading() {
  const dispatch = useDispatch();
  const { tradingPairs, selectedPair, orders } = useSelector((state) => state.trading);
  const [orderForm, setOrderForm] = useState({
    side: 'BUY',
    type: 'LIMIT',
    price: '',
    amount: '',
  });

  useEffect(() => {
    dispatch(fetchTradingPairs());
    dispatch(fetchUserOrders());
  }, [dispatch]);

  const handleCreateOrder = async (e) => {
    e.preventDefault();
    if (!selectedPair) return;

    const orderData = {
      tradingPairId: selectedPair.id,
      type: orderForm.type,
      side: orderForm.side,
      price: parseFloat(orderForm.price),
      amount: parseFloat(orderForm.amount),
    };

    await dispatch(createOrder(orderData));
    setOrderForm({ ...orderForm, price: '', amount: '' });
    dispatch(fetchUserOrders());
  };

  return (
    <div className="min-h-screen bg-gray-900">
      <Navbar />
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-3xl font-bold text-white mb-8">Trading</h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Trading Pairs */}
          <div className="bg-gray-800 p-6 rounded-lg">
            <h2 className="text-xl font-bold text-white mb-4">Markets</h2>
            <div className="space-y-2">
              {tradingPairs.map((pair) => (
                <div
                  key={pair.id}
                  onClick={() => dispatch(setSelectedPair(pair))}
                  className={`p-3 rounded cursor-pointer transition ${
                    selectedPair?.id === pair.id
                      ? 'bg-blue-600'
                      : 'bg-gray-700 hover:bg-gray-600'
                  }`}
                >
                  <div className="flex justify-between items-center">
                    <span className="text-white font-semibold">{pair.symbol}</span>
                    <span
                      className={
                        (pair.priceChangePercent24h || 0) >= 0
                          ? 'text-green-500'
                          : 'text-red-500'
                      }
                    >
                      {(pair.priceChangePercent24h || 0).toFixed(2)}%
                    </span>
                  </div>
                  <div className="text-gray-300 text-sm mt-1">
                    ${pair.currentPrice || '0.00'}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Order Form */}
          <div className="bg-gray-800 p-6 rounded-lg">
            <h2 className="text-xl font-bold text-white mb-4">
              Place Order {selectedPair && `- ${selectedPair.symbol}`}
            </h2>

            {!selectedPair ? (
              <p className="text-gray-400">Select a trading pair to start trading</p>
            ) : (
              <form onSubmit={handleCreateOrder} className="space-y-4">
                <div>
                  <label className="block text-gray-300 mb-2">Order Type</label>
                  <select
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={orderForm.type}
                    onChange={(e) => setOrderForm({ ...orderForm, type: e.target.value })}
                  >
                    <option value="LIMIT">Limit</option>
                    <option value="MARKET">Market</option>
                  </select>
                </div>

                <div>
                  <label className="block text-gray-300 mb-2">Side</label>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      onClick={() => setOrderForm({ ...orderForm, side: 'BUY' })}
                      className={`p-3 rounded font-bold ${
                        orderForm.side === 'BUY'
                          ? 'bg-green-600'
                          : 'bg-gray-700 hover:bg-gray-600'
                      }`}
                    >
                      Buy
                    </button>
                    <button
                      type="button"
                      onClick={() => setOrderForm({ ...orderForm, side: 'SELL' })}
                      className={`p-3 rounded font-bold ${
                        orderForm.side === 'SELL'
                          ? 'bg-red-600'
                          : 'bg-gray-700 hover:bg-gray-600'
                      }`}
                    >
                      Sell
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-gray-300 mb-2">Price</label>
                  <input
                    type="number"
                    step="0.00000001"
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={orderForm.price}
                    onChange={(e) => setOrderForm({ ...orderForm, price: e.target.value })}
                    placeholder={selectedPair.currentPrice || '0.00'}
                    required
                  />
                </div>

                <div>
                  <label className="block text-gray-300 mb-2">Amount</label>
                  <input
                    type="number"
                    step="0.00000001"
                    className="w-full p-3 rounded bg-gray-700 text-white border border-gray-600"
                    value={orderForm.amount}
                    onChange={(e) => setOrderForm({ ...orderForm, amount: e.target.value })}
                    placeholder="0.00"
                    required
                  />
                </div>

                <button
                  type="submit"
                  className={`w-full font-bold py-3 px-4 rounded transition ${
                    orderForm.side === 'BUY'
                      ? 'bg-green-600 hover:bg-green-700'
                      : 'bg-red-600 hover:bg-red-700'
                  }`}
                >
                  {orderForm.side === 'BUY' ? 'Buy' : 'Sell'} {selectedPair.baseCurrency}
                </button>
              </form>
            )}
          </div>

          {/* Open Orders */}
          <div className="bg-gray-800 p-6 rounded-lg">
            <h2 className="text-xl font-bold text-white mb-4">Open Orders</h2>
            {orders.length === 0 ? (
              <p className="text-gray-400">No open orders</p>
            ) : (
              <div className="space-y-2">
                {orders.filter((o) => o.status === 'PENDING' || o.status === 'PARTIALLY_FILLED').map((order) => (
                  <div key={order.id} className="bg-gray-700 p-3 rounded">
                    <div className="flex justify-between items-center mb-1">
                      <span className="text-white font-semibold">
                        {order.tradingPairSymbol}
                      </span>
                      <span
                        className={
                          order.side === 'BUY' ? 'text-green-500' : 'text-red-500'
                        }
                      >
                        {order.side}
                      </span>
                    </div>
                    <div className="text-gray-300 text-sm">
                      Price: ${order.price} | Amount: {order.amount}
                    </div>
                    <div className="text-gray-400 text-xs mt-1">
                      Filled: {order.filledAmount} / {order.amount}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Trading;

import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig'; 
import { useAuth } from '../context/AuthContext'; 
import { Lock, User, Loader2 } from 'lucide-react'; 
import { useNavigate } from 'react-router-dom'; // Add this

const LoginPage = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  
  useEffect(() => {
    console.log("inside use effect, is authenticated valuses ", isAuthenticated);
    if (isAuthenticated) {
      console.log("Authenticated! Redirecting to dashboard...");
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);
  

  const handleSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  setLoading(true);
  setError('');

  try {
    const response = await api.post('/auth/login', { username, password });
    
    // 1. Update the context
    
    login(response.data);
    
    console.log("Login successful!", response);
    
   
    
  } catch (err: any) {
    setError(err.response?.data?.message || 'Invalid username or password');
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-blue-900 to-slate-900">
      <div className="w-full max-w-md p-8 bg-white/10 backdrop-blur-lg rounded-2xl border border-white/20 shadow-2xl">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white mb-2">KuluBotti 🤖</h1>
          <p className="text-blue-200">AI-Powered Expense Tracking</p>
        </div>

        <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-500/20 border border-red-500 text-red-100 p-3 rounded-lg text-sm">
              {error}
            </div>
          )}

          <div className="relative">
            <User className="absolute left-3 top-3 text-blue-300 w-5 h-5" />
            <input
              type="text"
              placeholder="Username"
              className="w-full bg-white/5 border border-white/10 rounded-xl py-3 pl-11 pr-4 text-white placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className="relative">
            <Lock className="absolute left-3 top-3 text-blue-300 w-5 h-5" />
            <input
              type="password"
              placeholder="Password"
              className="w-full bg-white/5 border border-white/10 rounded-xl py-3 pl-11 pr-4 text-white placeholder-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-500 text-white font-bold py-3 rounded-xl shadow-lg shadow-blue-900/20 transition-all flex items-center justify-center gap-2"
          >
            {loading ? <Loader2 className="animate-spin" /> : 'Sign In'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
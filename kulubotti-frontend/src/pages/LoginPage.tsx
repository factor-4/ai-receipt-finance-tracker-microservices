import React, { useState, useEffect } from 'react';
import api from '../api/axiosConfig'; 
import { useAuth } from '../context/AuthContext'; 
import { Lock, User, Loader2 } from 'lucide-react'; 
import { useNavigate } from 'react-router-dom';

const LoginPage = () => {
  // Toggle between Login and Register modes
  const [isLoginMode, setIsLoginMode] = useState(true);
  
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState(''); // To show successful registration

  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard', { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      if (isLoginMode) {
        // Trigger Login Endpoint
        const response = await api.post('/auth/login', { username, password });
        login(response.data);
      } else {
        // Trigger Registration Endpoint
        await api.post('/auth/register', { username, password });
        setSuccessMsg('Account created successfully! You can now sign in.');
        setIsLoginMode(true); // Switch back to login view
        setPassword(''); // Clear password field for security
      }
    } catch (err: any) {
      // The Java backend returns plain string messages for errors
      setError(err.response?.data || err.response?.data?.message || 'An error occurred');
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
          {successMsg && (
            <div className="bg-green-500/20 border border-green-500 text-green-100 p-3 rounded-lg text-sm">
              {successMsg}
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
            {loading ? <Loader2 className="animate-spin" /> : (isLoginMode ? 'Sign In' : 'Create Account')}
          </button>
        </form>

        <div className="mt-6 text-center">
          <button
            type="button"
            onClick={() => {
              setIsLoginMode(!isLoginMode);
              setError('');
              setSuccessMsg('');
            }}
            className="text-blue-300 hover:text-white transition-colors text-sm"
          >
            {isLoginMode 
              ? "Don't have an account? Sign up" 
              : "Already have an account? Sign in"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
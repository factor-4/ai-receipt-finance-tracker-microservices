import React, { useState, useEffect } from 'react';
import { Plus, Receipt, Clock, CheckCircle, Loader2, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { expenseService } from '../api/expenseService';

interface Expense {
  id: string;
  merchantName: string;
  amount: number;
  status: 'PENDING' | 'PROCESSED';
  date: string;
}

const ExpenseDashboard = () => {
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(true);
  
  // 1. Destructure token alongside logout
  const { logout, token } = useAuth();
  
  // 2. Add state to hold the decoded username
  const [username, setUsername] = useState<string>('User');

  // The Ref (The "Remote Control")
  const fileInputRef = React.useRef<HTMLInputElement>(null);

  // 3. Decode the JWT to extract the username whenever the token changes
  useEffect(() => {
    if (token) {
      try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        const decodedToken = JSON.parse(jsonPayload);
        
        // Use 'sub' or 'username' depending on how your Spring Boot configures the JWT
        setUsername(decodedToken.sub || decodedToken.username || 'User');
      } catch (error) {
        console.error("Failed to decode token for username", error);
      }
    }
  }, [token]);

  // Initial load + Polling logic (Every 3 seconds)
  useEffect(() => {
    fetchExpenses();
    const interval = setInterval(fetchExpenses, 3000);
    return () => clearInterval(interval); // Clean up on close
  }, []);

  const fetchExpenses = async () => {
    try {
      const data = await expenseService.getExpenses();
      setExpenses(data);
    } catch (err) {
      console.error("Failed to fetch", err);
    } finally {
      setLoading(false);
    }
  };

  const handleFileUpload = async (file: File) => {
    setLoading(true);
    try {
      await expenseService.uploadReceipt(file);
      fetchExpenses(); 
    } catch (err) {
      alert("Upload failed. Check Gateway connection.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="flex justify-between items-start mb-12 bg-slate-900/50 p-6 rounded-2xl border border-white/5 backdrop-blur-md">
          <div>
            <h1 className="text-4xl font-black tracking-tight bg-gradient-to-r from-blue-400 via-indigo-400 to-emerald-400 bg-clip-text text-transparent">
              KuluBotti <span className="text-sm font-light text-slate-500 ml-2">v4.0</span>
            </h1>
            {/* 4. Display the dynamic username here */}
            <p className="text-slate-400 mt-2 font-medium">
              Welcome back, <span className="text-blue-400 font-bold capitalize">{username}</span>. Let's check your financial pulse.
            </p>
          </div>

          <div className="flex items-center gap-6">
            <button
              onClick={() => fileInputRef.current?.click()}
              className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white px-6 py-3 rounded-xl font-bold transition-all shadow-lg active:scale-95"
            >
              <Plus size={20} />
              New Receipt
            </button>

            {/* Separator Line */}
            <div className="h-10 w-[1px] bg-slate-800"></div>

            <button
              onClick={logout}
              className="flex flex-col items-center text-slate-500 hover:text-red-400 transition-colors group"
              title="Secure Logout"
            >
              <LogOut size={22} />
              <span className="text-[10px] mt-1 uppercase tracking-widest font-bold opacity-0 group-hover:opacity-100 transition-opacity">Exit</span>
            </button>
          </div>
        </div>

        {/* Expense List */}
        <div className="grid gap-4">
          {loading ? (
            <div className="flex justify-center p-12"><Loader2 className="animate-spin text-blue-500" size={40} /></div>
          ) : expenses.map((exp) => (
            <div key={exp.id} className="bg-slate-900 border border-slate-800 p-5 rounded-xl flex items-center justify-between hover:border-slate-700 transition-all shadow-lg">
              <div className="flex items-center gap-4">
                <div className="bg-slate-800 p-3 rounded-lg">
                  <Receipt className="text-blue-400" />
                </div>
                <div>
                  <h3 className="font-bold text-lg">{exp.merchantName}</h3>
                  <p className="text-sm text-slate-500">{exp.date}</p>
                </div>
              </div>

              <div className="flex items-center gap-8">
                <div className="text-right">
                  <p className="text-xl font-mono font-bold">€{exp.amount?.toFixed(2)}</p>
                  <div className="flex items-center gap-1 justify-end mt-1">
                    {exp.status === 'PENDING' ? (
                      <span className="text-amber-400 text-xs flex items-center gap-1">
                        <Clock size={12} className="animate-pulse" /> AI Processing...
                      </span>
                    ) : (
                      <span className="text-emerald-400 text-xs flex items-center gap-1">
                        <CheckCircle size={12} /> Processed
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Standardized Hidden Input: 
  - accept="image/*" ensures only images are picked.
  - capture="environment" tells mobile OS to open the rear camera.
      */}
      <input
        type="file"
        ref={fileInputRef}
        className="hidden"
        accept="image/*"
        capture="environment"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) handleFileUpload(file);
        }}
      />

    </div>
  );
};

export default ExpenseDashboard;
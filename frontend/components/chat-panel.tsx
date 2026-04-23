"use client";

import { useState } from 'react';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

type SourceSnippet = {
  filePath: string;
  symbol: string;
  startLine: number;
  endLine: number;
  snippet: string;
  score: number;
};

export default function ChatPanel() {
  const [repoUrl, setRepoUrl] = useState('');
  const [repoId, setRepoId] = useState<string | null>(null);
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [sources, setSources] = useState<SourceSnippet[]>([]);
  const [loading, setLoading] = useState(false);

  async function handleIngest() {
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/repos/ingest`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ repoUrl }),
      });
      const data = await res.json();
      setRepoId(data.repoId);
      alert('Ingestion started!');
    } catch (err) {
      console.error(err);
      alert('Error starting ingestion');
    } finally {
      setLoading(false);
    }
  }

  async function handleAsk() {
    if (!repoId) return alert('Please index a repository first');
    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/query`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ repoId, question }),
      });
      const data = await res.json();
      setAnswer(data.answer);
      setSources(data.sources || []);
    } catch (err) {
      console.error(err);
      alert('Error getting answer');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="space-y-8 animate-in fade-in duration-500">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <input
          type="text"
          placeholder="GitHub Repository URL"
          className="md:col-span-3 p-4 rounded-xl border-2 border-orange-100 focus:border-orange-500 focus:outline-none bg-white/80"
          value={repoUrl}
          onChange={(e) => setRepoUrl(e.target.value)}
        />
        <button
          onClick={handleIngest}
          disabled={loading}
          className="bg-orange-500 hover:bg-orange-600 text-white font-bold py-4 px-6 rounded-xl transition-all shadow-lg active:scale-95 disabled:opacity-50"
        >
          Index Repository
        </button>
      </div>

      <div className="flex flex-col space-y-4">
        <textarea
          placeholder="Ask a question about the code..."
          className="w-full p-6 rounded-2xl border-2 border-orange-100 focus:border-orange-500 focus:outline-none bg-white/80 h-32"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
        />
        <button
          onClick={handleAsk}
          disabled={loading || !repoId}
          className="bg-black hover:bg-gray-800 text-white font-bold py-4 px-6 rounded-xl transition-all shadow-lg active:scale-95 disabled:opacity-50"
        >
          Ask
        </button>
      </div>

      {answer && (
        <div className="space-y-6">
          <div className="bg-orange-50 p-8 rounded-3xl border border-orange-200">
            <h3 className="text-sm font-bold text-orange-600 uppercase tracking-widest mb-4">Answer</h3>
            <p className="text-gray-800 leading-relaxed font-sans">{answer}</p>
          </div>

          {sources.length > 0 && (
            <div className="space-y-4">
               <h3 className="text-sm font-bold text-gray-400 uppercase tracking-widest">Sources</h3>
               <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  {sources.map((src, i) => (
                    <div key={i} className="bg-white p-4 rounded-xl border border-gray-100 shadow-sm text-xs font-mono text-gray-600 truncate">
                      {src.filePath}:{src.startLine}-{src.endLine}
                    </div>
                  ))}
               </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

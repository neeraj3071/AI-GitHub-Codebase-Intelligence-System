"use client";

import { useState } from 'react';
import ChatPanel from '@/components/chat-panel';
import AnalyticsPanel from '@/components/analytics-panel';
import { clsx } from 'clsx';

export default function Home() {
  const [activeTab, setActiveTab] = useState<'chat' | 'analytics'>('chat');

  return (
    <main className="min-h-screen p-8 max-w-6xl mx-auto">
      <header className="mb-12 text-center">
        <h1 className="text-4xl font-extrabold mb-4 tracking-tight">AI GitHub Intelligence</h1>
        <p className="text-gray-600">Understand your codebase with RAG-powered insights.</p>
      </header>

      <div className="flex justify-center mb-8">
        <div className="inline-flex p-1 bg-gray-100 rounded-xl">
          <button
            onClick={() => setActiveTab('chat')}
            className={clsx(
              "px-6 py-2 rounded-lg font-medium transition-all",
              activeTab === 'chat' ? "bg-white shadow-md text-orange-600" : "text-gray-500 hover:text-gray-700"
            )}
          >
            Chat
          </button>
          <button
            onClick={() => setActiveTab('analytics')}
            className={clsx(
              "px-6 py-2 rounded-lg font-medium transition-all",
              activeTab === 'analytics' ? "bg-white shadow-md text-orange-600" : "text-gray-500 hover:text-gray-700"
            )}
          >
            Analytics
          </button>
        </div>
      </div>

      <div className="bg-white/50 backdrop-blur-sm rounded-3xl p-8 border border-white shadow-xl">
        {activeTab === 'chat' ? <ChatPanel /> : <AnalyticsPanel />}
      </div>
    </main>
  );
}

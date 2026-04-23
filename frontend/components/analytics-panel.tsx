export default function AnalyticsPanel() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 gap-8 animate-in fade-in duration-500">
      <div className="bg-white p-8 rounded-3xl border border-gray-100 shadow-sm space-y-6">
        <h3 className="text-xl font-bold">Language Breakdown</h3>
        <div className="space-y-4">
          {[
            { lang: 'Java', color: 'bg-orange-500', pct: '65%' },
            { lang: 'TypeScript', color: 'bg-blue-500', pct: '25%' },
            { lang: 'Python', color: 'bg-green-500', pct: '10%' },
          ].map((item) => (
            <div key={item.lang} className="space-y-1">
              <div className="flex justify-between text-sm font-medium">
                <span>{item.lang}</span>
                <span>{item.pct}</span>
              </div>
              <div className="w-full bg-gray-100 rounded-full h-2">
                <div 
                  className={`${item.color} h-2 rounded-full`} 
                  style={{ width: item.pct }}
                />
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="bg-white p-8 rounded-3xl border border-gray-100 shadow-sm">
        <h3 className="text-xl font-bold mb-6">Complexity Estimate</h3>
        <div className="flex items-center justify-center p-8 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
          <span className="text-5xl font-black text-orange-600">B+</span>
          <span className="ml-4 text-gray-400 font-medium">Maintainability Score</span>
        </div>
      </div>

      <div className="md:col-span-2 bg-white p-8 rounded-3xl border border-gray-100 shadow-sm">
        <h3 className="text-xl font-bold mb-4">Module Dependency Graph</h3>
        <div className="aspect-video bg-gray-900 rounded-2xl flex items-center justify-center overflow-hidden">
          <div className="text-gray-500 font-mono text-sm leading-relaxed">
             [repo-ingestion] --(kafka)--&gt; [code-chunking] --(kafka)--&gt; [embedding] <br/>
             &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br/>
             &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[vector-store]
          </div>
        </div>
      </div>

      <div className="md:col-span-2 bg-white p-8 rounded-3xl border border-gray-100 shadow-sm">
        <h3 className="text-xl font-bold mb-6">Most Referenced Files</h3>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
           {['KafkaConfig.java', 'ChunkingService.java', 'RepoUris.ts'].map(f => (
             <div key={f} className="p-4 bg-orange-50 border border-orange-100 rounded-xl font-mono text-sm text-orange-700">
               {f}
             </div>
           ))}
        </div>
      </div>
    </div>
  );
}

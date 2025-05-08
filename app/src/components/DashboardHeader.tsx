
import React from 'react';
import { Music2 } from 'lucide-react';

const DashboardHeader: React.FC = () => {
  return (
    <div className="flex items-center gap-3 mb-6">
      <div className="p-2 rounded-lg bg-primary/20 text-primary">
        <Music2 className="h-6 w-6" />
      </div>
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Lyrics Genre Classifier</h1>
        <p className="text-muted-foreground text-sm">
          Analyze song lyrics to predict music genres using machine learning
        </p>
      </div>
    </div>
  );
};

export default DashboardHeader;

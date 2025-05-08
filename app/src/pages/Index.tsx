import React, { useState } from "react";
import DashboardHeader from "@/components/DashboardHeader";
import LyricsInput from "@/components/LyricsInput";
import GenreDisplay from "@/components/GenreDisplay";
import GenreChart from "@/components/GenreChart";
import { Card } from "@/components/ui/card";
import LyricsAnalysisStats from "@/components/LyricsAnalysisStats";
import { predictLyrics, GenrePrediction } from "@/services/api";
import { toast } from "sonner";

const Index: React.FC = () => {
	const [prediction, setPrediction] = useState<GenrePrediction | null>(null);
	const [isLoading, setIsLoading] = useState(false);
	const [currentLyrics, setCurrentLyrics] = useState<string | null>(null);

	const handleLyricsSubmit = async (lyrics: string) => {
		setIsLoading(true);
		setCurrentLyrics(lyrics);
		try {
			const result = await predictLyrics(lyrics);
			if (result) {
				setPrediction(result);
				toast.success("Lyrics successfully analyzed!");
			}
		} catch (error) {
			console.error("Error analyzing lyrics:", error);
			toast.error("Failed to analyze lyrics. Please try again.");
		} finally {
			setIsLoading(false);
		}
	};

	return (
		<div className="min-h-screen bg-dashboard-bg text-foreground py-8 px-4 sm:px-6 md:px-8 flex items-center justify-center">
			<div className="container max-w-7xl mx-auto">
				<DashboardHeader />

				<div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
					{/* Input Section */}
					<Card className="card-gradient p-6 pb-4 lg:col-span-1">
						<LyricsInput onSubmit={handleLyricsSubmit} isLoading={isLoading} />
						<LyricsAnalysisStats lyrics={currentLyrics} isLoading={isLoading} />
					</Card>

					{/* Results Section */}
					<div className="lg:col-span-2 space-y-6">
						<GenreDisplay prediction={prediction} isLoading={isLoading} />
						<GenreChart prediction={prediction} isLoading={isLoading} />
					</div>
				</div>
			</div>
		</div>
	);
};

export default Index;

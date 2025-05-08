import React, { useMemo } from "react";
import { Card } from "@/components/ui/card";
import {
	BarChartIcon,
	ListIcon,
	Tally5,
	CaseUpperIcon,
	WholeWordIcon,
} from "lucide-react";

interface LyricsAnalysisStatsProps {
	lyrics: string | null;
	isLoading: boolean;
}

const LyricsAnalysisStats: React.FC<LyricsAnalysisStatsProps> = ({
	lyrics,
	isLoading,
}) => {
	const stats = useMemo(() => {
		if (!lyrics) return null;

		const lines = lyrics.split("\n").filter((line) => line.trim().length > 0);

		// All words (including duplicates)
		const allWords = lyrics
			.toLowerCase()
			.replace(/[^\w\s]/g, "")
			.split(/\s+/)
			.filter((word) => word.length > 0);

		// Unique words
		const uniqueWords = new Set(allWords);

		// Average word length
		const totalCharacters = allWords.reduce(
			(sum, word) => sum + word.length,
			0
		);
		const avgWordLength = totalCharacters / allWords.length || 0;

		return {
			lines: lines.length,
			wordCount: allWords.length,
			uniqueWordCount: uniqueWords.size,
			avgWordLength: avgWordLength.toFixed(1),
			repetitionRate: ((1 - uniqueWords.size / allWords.length) * 100).toFixed(
				1
			),
		};
	}, [lyrics]);

	if (!stats || isLoading) {
		return <></>;
	}

	return (
		<Card className="card-gradient p-6 mt-6">
			{/* <h3 className="text-lg font-medium text-muted-foreground mb-4 text-center">
				Lyrics Analysis Stats
			</h3> */}

			<div className="grid grid-cols-2 md:grid-cols-3 gap-4">
				<StatCard
					icon={<Tally5 className="h-4 w-4" />}
					title="Word Count"
					value={stats.wordCount}
				/>
				<StatCard
					icon={<ListIcon className="h-4 w-4" />}
					title="Line Count"
					value={stats.lines}
				/>
				<StatCard
					icon={<CaseUpperIcon className="h-4 w-4" />}
					title="Unique Words"
					value={stats.uniqueWordCount}
				/>
				<StatCard
					icon={<WholeWordIcon className="h-4 w-4" />}
					title="Avg Word Length"
					value={`${stats.avgWordLength} chars`}
				/>
				<StatCard
					icon={<BarChartIcon className="h-4 w-4" />}
					title="Repetition Rate"
					value={`${stats.repetitionRate}%`}
				/>
			</div>
		</Card>
	);
};

interface StatCardProps {
	icon: React.ReactNode;
	title: string;
	value: number | string;
}

const StatCard: React.FC<StatCardProps> = ({ icon, title, value }) => {
	return (
		<div className="bg-white/50 rounded-md p-3 text-center flex flex-col items-center justify-center">
			<div className="flex items-center justify-center mb-1 text-primary">
				{icon}
			</div>
			<p className="text-xs text-muted-foreground">{title}</p>
			<p className="mt-1 text-sm font-semibold leading-none">{value}</p>
		</div>
	);
};

export default LyricsAnalysisStats;

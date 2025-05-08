import React from "react";
import { Card } from "@/components/ui/card";
import { GenrePrediction } from "@/services/api";
import { Spinner } from "./ui/spinner";
interface GenreDisplayProps {
	prediction: GenrePrediction | null;
	isLoading: boolean;
}

const GenreDisplay: React.FC<GenreDisplayProps> = ({
	prediction,
	isLoading,
}) => {
	if (!prediction) {
		return (
			<Card className="card-gradient p-6 flex flex-col items-center justify-center space-y-2 min-h-[120px]">
				{isLoading && <Spinner className="h-6 w-6 text-muted-foreground" />}
				{!isLoading && (
					<p className="text-muted-foreground text-center">
						Enter lyrics to get genre prediction
					</p>
				)}
			</Card>
		);
	}

	const genreColorMap: Record<string, string> = {
		pop: "bg-genre-pop",
		country: "bg-genre-country",
		blues: "bg-genre-blues",
		jazz: "bg-genre-jazz",
		reggae: "bg-genre-reggae",
		rock: "bg-genre-rock",
		"hip hop": "bg-genre-hip-hop",
		rnb: "bg-genre-rnb",
	};

	const genreMap: Record<string, string> = {
		pop: "Pop ⭐️",
		country: "Country 🤠",
		blues: "Blues 🎸",
		jazz: "Jazz 🎷",
		reggae: "Reggae 🌴",
		rock: "Rock 🤘",
		"hip hop": "Hip Hop 🎤",
		rnb: "R&B 🎶",
	};

	const genreProbabilityMap: Record<string, string> = {
		pop: "popProbability",
		country: "countryProbability",
		blues: "bluesProbability",
		jazz: "jazzProbability",
		reggae: "reggaeProbability",
		rock: "rockProbability",
		"hip hop": "hipHopProbability",
		rnb: "rnbProbability",
	};

	const colorClass =
		genreColorMap[prediction.genre.toLowerCase()] || "bg-primary";
	const confidence =
		prediction[genreProbabilityMap[prediction.genre.toLowerCase()]] * 100;

	if (isLoading) {
		return (
			<Card className="card-gradient p-6 flex flex-col items-center justify-center space-y-2 min-h-[120px]">
				<Spinner className="h-6 w-6 text-muted-foreground" />
			</Card>
		);
	}

	return (
		<Card className="card-gradient p-6 flex flex-col space-y-4">
			<div className="text-center space-y-2">
				<h3 className="text-lg font-medium text-muted-foreground">
					Top Predicted Genre
				</h3>
				<div className="flex items-center justify-center">
					<div className={`${colorClass} h-3 w-3 rounded-full mr-2`}></div>
					<h2 className="text-3xl font-bold capitalize">
						{genreMap[prediction.genre] || prediction.genre}
					</h2>
				</div>
			</div>

			<div className="bg-secondary/50 rounded-full h-3 w-full overflow-hidden">
				<div
					className={`${colorClass} h-full animate-pulse-slow`}
					style={{ width: `${confidence}%` }}
				></div>
			</div>

			<p className="text-center font-medium">
				{confidence.toFixed(1)}% confidence
			</p>
		</Card>
	);
};

export default GenreDisplay;

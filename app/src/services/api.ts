import { toast } from "sonner";

export interface GenrePrediction {
	genre: string;
	popProbability: number;
	countryProbability: number;
	bluesProbability: number;
	jazzProbability: number;
	reggaeProbability: number;
	rockProbability: number;
	hipHopProbability: number;
	rnbProbability: number;
}

export async function predictLyrics(
	lyrics: string
): Promise<GenrePrediction | null> {
	try {
		const response = await fetch("http://localhost:9090/lyrics/predict", {
			method: "POST",
			headers: {
				"Content-Type": "text/plain",
			},
			body: lyrics,
		});

        console.log(response);

		if (!response.ok) {
			throw new Error("Failed to predict lyrics");
		}

		return await response.json();
	} catch (error) {
		console.error("Error predicting lyrics:", error);
		toast.error("Failed to analyze lyrics. Is the server running?");
		return null;
	}
}

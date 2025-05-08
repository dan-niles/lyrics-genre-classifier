import React from "react";
import { Card } from "@/components/ui/card";
import { GenrePrediction } from "@/services/api";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { ChartBarIcon, ChartPieIcon } from "lucide-react";
import Highcharts from "highcharts";
import HighchartsReact from "highcharts-react-official";

interface GenreChartProps {
	prediction: GenrePrediction | null;
}

const GenreChart: React.FC<GenreChartProps> = ({ prediction }) => {
	if (!prediction) {
		return (
			<Card className="card-gradient p-6 flex items-center justify-center min-h-[320px]">
				<p className="text-muted-foreground">No data to display</p>
			</Card>
		);
	}

	const prediction_probabilities = {
		Pop: prediction.popProbability,
		Country: prediction.countryProbability,
		Blues: prediction.bluesProbability,
		Jazz: prediction.jazzProbability,
		Reggae: prediction.reggaeProbability,
		Rock: prediction.rockProbability,
		"Hip Hop": prediction.hipHopProbability,
		"R&B": prediction.rnbProbability,
	};

	const chartData = Object.entries(prediction_probabilities)
		.map(([name, value]) => ({
			name: name.charAt(0).toUpperCase() + name.slice(1),
			value: parseFloat((value * 100).toFixed(1)),
		}))
		.sort((a, b) => b.value - a.value);

	const GENRE_COLORS: Record<string, string> = {
		Pop: "#F86705",
		Country: "#FFB831",
		Blues: "#1267E5",
		Jazz: "#FF4460",
		Reggae: "#32CD32",
		Rock: "#DC143C",
		"Hip Hop": "#9932CC",
		// maroon color
		"R&B": "#800000",
	};

	const CustomTooltip: React.FC<{
		active?: boolean;
		payload?: { name: string; value: number }[];
	}> = ({ active, payload }) => {
		if (active && payload && payload.length) {
			return (
				<div className="bg-dashboard-card/90 backdrop-blur p-2 border border-border rounded shadow-lg">
					<p className="font-medium">{payload[0].name}</p>
					<p className="text-sm">{`${payload[0].value}%`}</p>
				</div>
			);
		}
		return null;
	};

	return (
		<Card className="card-gradient p-6">
			<Tabs defaultValue="bar">
				<div className="flex justify-between items-center mb-6">
					<h3 className="text-lg font-medium">Genre Probability</h3>
					<TabsList className="bg-dashboard-card/40">
						<TabsTrigger
							value="bar"
							className="data-[state=active]:bg-primary data-[state=active]:text-white"
						>
							<ChartBarIcon className="h-4 w-4 mr-1" />
							Bar
						</TabsTrigger>
						<TabsTrigger
							value="pie"
							className="data-[state=active]:bg-primary data-[state=active]:text-white"
						>
							<ChartPieIcon className="h-4 w-4 mr-1" />
							Pie
						</TabsTrigger>
					</TabsList>
				</div>

				<TabsContent value="bar" className="mt-0">
					<div className="w-full h-[420px]">
						<HighchartsReact
							highcharts={Highcharts}
							options={{
								chart: {
									type: "bar",
									backgroundColor: "transparent",
								},
								title: {
									text: null,
								},
								xAxis: {
									categories: chartData.map((entry) => entry.name),
									title: null,
									labels: {
										style: { color: "#404040" },
									},
								},
								yAxis: {
									min: 0,
									max: 100,
									title: {
										text: "Probability (%)",
										align: "high",
										style: { color: "#888" },
									},
									labels: {
										overflow: "justify",
										format: "{value}%",
										style: { color: "#888" },
									},
								},
								tooltip: {
									valueSuffix: "%",
								},
								plotOptions: {
									bar: {
										dataLabels: {
											enabled: true,
											format: "{y}%",
										},
										colorByPoint: true,
									},
								},
								legend: {
									enabled: false,
								},
								credits: {
									enabled: false,
								},
								series: [
									{
										name: "Probability",
										data: chartData.map((entry) => ({
											name: entry.name,
											y: entry.value,
											color: GENRE_COLORS[entry.name] || "#8B5CF6",
										})),
									},
								],
							}}
						/>
					</div>
				</TabsContent>

				<TabsContent value="pie" className="mt-0">
					<div className="w-full h-[420px]">
						<HighchartsReact
							highcharts={Highcharts}
							options={{
								chart: {
									type: "pie",
									backgroundColor: "transparent",
								},
								title: {
									text: null,
								},
								tooltip: {
									valueSuffix: "%",
								},
								plotOptions: {
									pie: {
										align: "center",
										verticalAlign: "middle",
										size: "100%",
										allowPointSelect: true,
										cursor: "pointer",
										dataLabels: [
											{
												enabled: true,
												distance: 40,
												formatter: function () {
													return (
														"<b>" +
														this.point.name +
														"</b><br/> " +
														Math.round(this.point.y).toFixed(1) +
														"%"
													);
												},
												style: {
													fontSize: "0.8em",
													fontWeight: "normal",
													color: "#404040",
													opacity: 1,
												},
											},
										],
									},
								},
								series: [
									{
										name: "Probability",
										colorByPoint: true,
										data: chartData.map((entry) => ({
											name: entry.name,
											y: entry.value,
											color: GENRE_COLORS[entry.name] || "#8B5CF6",
										})),
									},
								],
							}}
						/>
					</div>
				</TabsContent>
			</Tabs>
		</Card>
	);
};

export default GenreChart;

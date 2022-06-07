echo "Start building mvp tools.."
find src -name "*.java" > sources.txt
javac -cp "lib/*" -d classes @sources.txt
jar cfm Frontend.jar Frontend.txt -C classes bftsmart
jar cfm Replica.jar Replica.txt -C classes bftsmart
jar cfm ThroughputLatencyClient.jar ThroughputLatencyClient.txt -C classes bftsmart
jar cfm ThroughputLatencyServer.jar ThroughputLatencyServer.txt -C classes bftsmart
echo "Finished Compiling"
for jar in Frontend.jar Replica.jar ThroughputLatencyClient.jar ThroughputLatencyServer.jar; do
	for replica in rasp0.local rasp1.local rasp2.local rasp3.local rasp4.local rasp5.local; do
		scp $jar cb@$replica:~/git/sorrir-framework/resilience_library/bft_smart
		#konsole -e "ssh $replica 'mkdir bftsmart'"  &
	done
done
echo "Copied files to raspberries"
for jar in Frontend.jar Replica.jar ThroughputLatencyClient.jar ThroughputLatencyServer.jar; do
	for replica in rasp0.local rasp1.local rasp2.local rasp3.local rasp4.local rasp5.local; do
		scp $jar cb@$replica:~/bftsmart
	done
done
echo "Copying configs.."
for replica in rasp0.local rasp1.local rasp2.local rasp3.local rasp4.local rasp5.local; do
	scp -r 4raspberries/config cb@$replica:~/bftsmart
done

echo "Copying lib.."
for replica in rasp0.local rasp1.local rasp2.local rasp3.local rasp4.local rasp5.local; do
	scp -r lib cb@$replica:~/bftsmart
done

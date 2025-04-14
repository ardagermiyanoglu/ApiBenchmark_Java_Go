package main

import (
	"fmt"
	"net/http"
	"sync"
	"time"
)

func main() {
	url := "https://ardagermiyanoglu.github.io/"
	totalRequests := 100
	concurrency := 10

	var wg sync.WaitGroup
	var mu sync.Mutex

	successCount := 0
	failureCount := 0
	totalDuration := time.Duration(0)

	sem := make(chan struct{}, concurrency)

	startTime := time.Now()

	for i := 0; i < totalRequests; i++ {
		wg.Add(1)
		sem <- struct{}{} // concurrency kontrolü

		go func() {
			defer func() {
				<-sem // slot boşalt
				wg.Done()
			}()

			start := time.Now()
			resp, err := http.Get(url)
			duration := time.Since(start)

			mu.Lock()
			totalDuration += duration
			if err == nil && resp.StatusCode == 200 {
				successCount++
			} else {
				failureCount++
			}
			mu.Unlock()

			if resp != nil {
				resp.Body.Close()
			}
		}()
	}

	wg.Wait()
	elapsed := time.Since(startTime)

	average := totalDuration / time.Duration(totalRequests)
	qps := float64(totalRequests) / elapsed.Seconds()

	fmt.Println("=================================")
	fmt.Printf("✅ Toplam İstek: %d\n", totalRequests)
	fmt.Printf("✅ Başarılı: %d\n", successCount)
	fmt.Printf("❌ Başarısız: %d\n", failureCount)
	fmt.Printf("⏱️ Ortalama Süre: %v\n", average)
	fmt.Printf("📊 QPS: %.2f\n", qps)
	fmt.Printf("⏳ Toplam Süre: %v\n", elapsed)
	fmt.Println("=================================")
}

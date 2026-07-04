<script setup lang="ts">
import Autoplay from 'embla-carousel-autoplay'
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from '@/components/ui/carousel'

const images = [
  { src: '/assets/carousel/img1.png', alt: 'Image 1', caption: 'Tất cả trong một. Không còn tab thừa' },
  { src: '/assets/carousel/img2.png', alt: 'Image 2', caption: 'Nơi quyết định được đưa ra cùng nhau' },
  { src: '/assets/carousel/img4.png', alt: 'Image 4', caption: 'Dù xa hay gần — cùng một cuộc họp' },
  { src: '/assets/carousel/img5.png', alt: 'Image 5', caption: 'Thoải mái làm việc. Không mất kết nối' },
]

const plugin = Autoplay({
  delay: 2000,
  stopOnMouseEnter: true,
  stopOnInteraction: false,
})
</script>

<template>
  <div class="flex min-h-svh w-full items-center justify-center background">
    <Carousel class="relative w-full max-w-full" :opts="{ loop: true }" :plugins="[plugin]">
      <CarouselContent>
        <CarouselItem v-for="(img, index) in images" :key="index">
          <div class="relative flex items-center justify-center min-h-svh">
            <img :src="img.src" :alt="img.alt" class="w-auto h-[70vh] syn-img" />
            <div class="absolute bottom-16 left-0 right-0 text-center px-8">
              <h1 class="text-white text-3xl md:text-5xl font-semibold tracking-tight drop-shadow-lg quote">
                {{ img.caption }}
              </h1>
            </div>
          </div>
        </CarouselItem>
      </CarouselContent>

      <CarouselNext class="right-4 top-1/2 -translate-y-1/2" />
      <CarouselPrevious class="left-4 top-1/2 -translate-y-1/2" />
    </Carousel>
  </div>
</template>

<style scoped>
.syn-img {
  position: relative;
  z-index: 10;
  filter: drop-shadow(0 0 20px var(--secondary));
  object-fit: contain;
}

.quote {
  font-family: "Coiny", system-ui;
  font-weight: 400;
  font-style: normal;
}
</style>
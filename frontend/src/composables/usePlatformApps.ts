import { onMounted, ref } from 'vue'
import { listPlatformApps } from '@/api/apps'
import type { PlatformApp } from '@/types/api'
import { DEMO_APPS } from '@/constants/demoApps'

const apps = ref<PlatformApp[]>([])
const loaded = ref(false)
let loadingPromise: Promise<void> | null = null

function fallbackApps(): PlatformApp[] {
  return DEMO_APPS.map((item) => ({
    code: item.id,
    name: item.name,
    tagline: item.tagline,
    description: item.description,
    expertId: 'home-health-advisor',
  }))
}

export function usePlatformApps() {
  async function ensureLoaded() {
    if (loaded.value) {
      return
    }
    if (!loadingPromise) {
      loadingPromise = (async () => {
        try {
          apps.value = await listPlatformApps()
        } catch {
          apps.value = fallbackApps()
        } finally {
          loaded.value = true
        }
      })()
    }
    await loadingPromise
  }

  onMounted(() => {
    void ensureLoaded()
  })

  function findApp(code: string): PlatformApp | undefined {
    return apps.value.find((item) => item.code === code)
  }

  return { apps, loaded, ensureLoaded, findApp }
}

import { createClient } from '@supabase/supabase-js'
const supabase_url = 'https://dhwevbvoicvxgauqibna.supabase.co'
const anon_key= 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRod2V2YnZvaWN2eGdhdXFpYm5hIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA5Nzc1OTksImV4cCI6MjA3NjU1MzU5OX0.BaZLUZOzty-NVfcbt_FSCpvk0TH090nDmL2ursVk3wY'

// Create a single supabase client for interacting with your database
const supabase = createClient(supabase_url, anon_key)
const { data, error } = await supabase.functions.invoke('hello')

if (error) {
  console.error('❌ Function failed:', error)
} else {
  console.log('✅ Function works:', data)
}

// Unified API exports
export { loginApi, registerApi, getUserInfoApi, updateProfileApi, updatePasswordApi } from './auth'
export { chatStream } from './chat'
export { fetchHollandQuestions, submitHollandTest, interpretHollandStream } from './interestTest'
export { matchAdmission, fetchScoreInsights, fetchHollandMajorMap, checkAnalyticsHealth } from './analytics'
export { generateVolunteerBrief, generateHollandBrief, generateSchoolCompareReport, checkReportHealth } from './report'
export { fetchVolunteerPlans, saveVolunteerPlan, deleteVolunteerPlan, reviewPlanStream } from './plan'
export { lookupScoreRank } from './rank'
export { fetchWizardProgress, completeWizardStep, saveWizardStep, focusWizardStep } from './wizard'
export { fetchFavoriteSchools, addFavoriteSchool, removeFavoriteSchool, checkFavoriteSchool } from './favorite'
export { fetchDashboardStats } from './stats'
export {
  fetchSchoolPage,
  fetchSchoolOptions,
  fetchSchoolLocations,
  fetchSchoolDetail,
  fetchSchoolCompare,
  fetchSchoolMapMarkers,
} from './school'
export {
  fetchMajorPage,
  fetchMajorCategories,
  fetchMajorDetail,
  fetchMajorCompare,
} from './major'

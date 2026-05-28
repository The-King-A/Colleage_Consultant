import request from './request'

export function lookupScoreRank({ province, subjectType, score, year }) {
  return request.get('/rank/lookup', {
    params: { province, subjectType, score, year },
  })
}

# AI-MAS-Assignment3
The third and final project, for the 02285 Artificial Intelligence and Multi-Agent Systems.

## Description
The project, in all its simplicity, is about planning. More specifically the actions of so-called agents, to solve goals by proximity.

## Notes
offline/online planning?


independent agents (multi threading)

hericical approach

assume sub-goal independence

forward search
	best first search
	heuristic

negotiation
	highest workload = highest priority
	bidding for who solves the job
	
	
## Tasks

### Sub-Goals
*  [x] Adjustable Resolution
*  [x] Goal state
*  [x] H: variable goal state
*  [x] K cluster



### Conflict Analysis
*  [x] Identify a problem
*  [x] Describe the problem
  * [x] 2  search
    *  [x] Realistic
    *  [x] Relaxed        
  *  [x] DS: route
*  [ ] Predict a problem

### Conflict Resolution
*  [-] Agent
  *  [x] Msg: "Please move"
  *  [ ] Who moves least
*  [x] Box
  *  [x] Who can move it?

### Heuristic
*  [ ] Parring box to goal?
*  [ ] Clustering of goals.
  *  [x] Clustering on color
	
### Work list
*  [x] Refactor Agent - Kasper
*  [-] Relaxed by number of colours - Kasper
*  [x] Refactor Map - Daniel
*  [x] Route - Martin
*  [ ] Heuristic - Martin
*  [ ] Speed, profiler- martin
*  [x] Conflict - Kasper og Daniel
*  [x] Optimize node hashcode - daniel
*  [x] Optimize node equals - daniel
*  [ ] Create expand of node, only using box-moves.
*  [ ] Plan longer move for helper agent.
*  [ ] NoOPs into stuck agent, for Helper Agent's route to the box in question.
*  [ ] Identify "safe spot" to stash Helper Agent and box, while stuck agent moves.
*  [ ] Reduce memory footprint
  *  [ ] Reduce the seach space?
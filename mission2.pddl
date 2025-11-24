;; mission2.pddl - Mission 2
(define (problem mission2)
  (:domain lunar-mission)
  (:objects 
    lander1 lander2 - lander
    rover1 rover2 - rover
    wp1 wp2 wp3 wp4 wp5 wp6 - waypoint
    img scn - data_type
  )

  (:init
    ;; Rover 1 deployed at wp2
    (at rover1 wp2)
    (lander_at lander1 wp2)
    (associated rover1 lander1)
    (memory_clear rover1)
    
    ;; Rover 2 undeployed
    (in_lander rover2 lander2)
    (lander_at lander2 wp5)
    (associated rover2 lander2)

    ;; Map connectivity (Figure 3)
    (connected wp1 wp2) (connected wp2 wp1)
    (connected wp2 wp3)
    (connected wp2 wp4)
    (connected wp3 wp5)
    (connected wp4 wp2)
    (connected wp5 wp3)
    (connected wp5 wp6)
    (connected wp6 wp4)
  )

  (:goal (and 
    (data_transmitted img wp3)
    (data_transmitted scn wp4)
    (data_transmitted img wp2)
    (data_transmitted scn wp6)
    (sample_collected wp5)
    (sample_collected wp1)
  ))
)
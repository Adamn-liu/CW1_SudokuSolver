;; mission1.pddl - Mission 1
(define (problem mission1)
  (:domain lunar-mission)
  (:objects 
    lander1 - lander
    rover1 - rover
    wp1 wp2 wp3 wp4 wp5 - waypoint
    img scn - data_type
  )

  (:init
    (in_lander rover1 lander1)
    (lander_at lander1 wp1)
    (associated rover1 lander1)
    
    (connected wp1 wp2)
    (connected wp1 wp4)
    (connected wp2 wp3)
    (connected wp3 wp5)
    (connected wp4 wp3)
    (connected wp5 wp1)
  )

  (:goal (and 
    (data_transmitted img wp5)
    (data_transmitted scn wp3)
    (sample_collected wp1)
  ))
)

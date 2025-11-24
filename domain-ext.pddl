(define (domain lunar-mission-extended)
  (:requirements :strips :typing)
  (:types 
    lander rover waypoint - object
    data_type - object
    astronaut room_type - object
  )
  
  (:predicates
    (at ?r - rover ?w - waypoint)
    (lander_at ?l - lander ?w - waypoint)
    (in_lander ?r - rover ?l - lander)
    (connected ?from ?to - waypoint)
    (associated ?r - rover ?l - lander)
    
    (memory_clear ?r - rover)
    (have_data ?r - rover ?d - data_type ?w - waypoint)
    (carrying_sample ?r - rover ?w - waypoint)
    (sample_capacity_full ?r - rover)
    (lander_full ?l - lander)
    
    (data_transmitted ?d - data_type ?w - waypoint)
    (sample_collected ?w - waypoint)

    (at_astronaut ?a - astronaut ?rm - room_type ?l - lander)
  )

  (:action move_astronaut
    :parameters (?a - astronaut ?l - lander ?from - room_type ?to - room_type)
    :precondition (at_astronaut ?a ?from ?l)
    :effect (and 
        (not (at_astronaut ?a ?from ?l))
        (at_astronaut ?a ?to ?l)
    )
  )

  (:action deploy
    :parameters (?r - rover ?l - lander ?w - waypoint ?a - astronaut)
    :precondition (and 
        (in_lander ?r ?l)
        (lander_at ?l ?w)
        (at_astronaut ?a docking ?l)
    )
    :effect (and 
        (not (in_lander ?r ?l))
        (at ?r ?w)
        (memory_clear ?r)
        (not (sample_capacity_full ?r))
    )
  )

  (:action navigate
    :parameters (?r - rover ?from ?to - waypoint)
    :precondition (and (at ?r ?from) (connected ?from ?to))
    :effect (and (not (at ?r ?from)) (at ?r ?to))
  )

  (:action take_image
    :parameters (?r - rover ?w - waypoint)
    :precondition (and (at ?r ?w) (memory_clear ?r))
    :effect (and (not (memory_clear ?r)) (have_data ?r img ?w))
  )

  (:action perform_scan
    :parameters (?r - rover ?w - waypoint)
    :precondition (and (at ?r ?w) (memory_clear ?r))
    :effect (and (not (memory_clear ?r)) (have_data ?r scn ?w))
  )

  (:action transmit_data
    :parameters (?r - rover ?l - lander ?d - data_type ?w - waypoint ?a - astronaut)
    :precondition (and 
        (associated ?r ?l)
        (have_data ?r ?d ?w)
        (at_astronaut ?a control ?l)
    )
    :effect (and 
        (not (have_data ?r ?d ?w))
        (memory_clear ?r)
        (data_transmitted ?d ?w)
    )
  )

  (:action collect_sample
    :parameters (?r - rover ?w - waypoint)
    :precondition (and (at ?r ?w) (not (sample_capacity_full ?r)))
    :effect (and (sample_capacity_full ?r) (carrying_sample ?r ?w))
  )

  (:action drop_sample
    :parameters (?r - rover ?l - lander ?w_lander - waypoint ?w_sample - waypoint ?a - astronaut)
    :precondition (and 
        (at ?r ?w_lander)
        (lander_at ?l ?w_lander)
        (carrying_sample ?r ?w_sample)
        (not (lander_full ?l))
        (at_astronaut ?a docking ?l)
    )
    :effect (and 
        (not (carrying_sample ?r ?w_sample))
        (not (sample_capacity_full ?r))
        (lander_full ?l)
        (sample_collected ?w_sample)
    )
  )
)
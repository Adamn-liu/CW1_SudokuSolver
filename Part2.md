## Part 2: Automated Planning – PDDL Domain and Problem Modelling

### 2.1 Domain Modelling (Part 2A)

We modelled the lunar mission using **PDDL 1.2** with `:strips` and `:typing` requirements, strictly adhering to the constraints in the coursework:

- The **Lander is stationary** after landing.
- The **Rover can store only one data item** (`image` or `scan`) at a time.
- **Physical samples must be returned manually** to the Lander.
- Each **Lander can store only one sample**.
- **Data transmission is wireless** (no proximity needed), but **sample delivery requires physical presence**.

#### Object Types

We defined the following types to capture mission semantics:

- `lander`, `rover`, `waypoint`: Core physical entities.
- `data_type`: Abstract type for data (`img` = image, `scn` = scan).
- *(Part 2C only)* `astronaut`, `room_type`: Humans and internal lander zones (`control`, `docking`).

#### Predicates

Key predicates represent the world state:

- **Location**: `at`, `lander_at`, `in_lander`, `connected`
- **Association**: `associated` (binds rover to its lander)
- **Resource states**: `memory_clear`, `sample_capacity_full`, `lander_full`
- **Mission progress**: `have_data`, `carrying_sample`, `data_transmitted`, `sample_collected`
- *(Part 2C only)* `at_astronaut ?a - astronaut ?room - room_type ?l - lander`

#### Actions

We implemented the following actions with precise preconditions and effects:

- `deploy`: Releases rover from lander. Requires rover inside lander and lander at waypoint.
- `navigate`: Moves rover between connected waypoints.
- `take_image` / `perform_scan`: Captures data if memory is clear.
- `transmit_data`: Sends data to **associated** lander (wireless, no location constraint).
- `collect_sample`: Picks up sample if rover’s sample slot is free.
- `drop_sample`: Delivers sample to lander (requires rover at lander location and lander not full).
- *(Part 2C only)* `move_astronaut`: Switches astronaut between `control` and `docking`.

In **Part 2C**, we added astronaut presence constraints:

- `deploy` and `drop_sample` require an astronaut in the **docking bay**.
- `transmit_data` requires an astronaut in the **control room**.

All actions respect resource limits (e.g., cannot collect two samples, cannot store two data items).

---

### 2.2 Problem Instances (Part 2B & 2C)

#### Mission 1

- **Initial state**: `rover1` undeployed inside `lander1` at `wp1`.
- **Map**: Directed graph per **Figure 2** (e.g., `wp1 → wp2`, `wp2 → wp3`, `wp5 → wp1`).
- **Goals**:
  - `(data_transmitted img wp5)`
  - `(data_transmitted scn wp3)`
  - `(sample_collected wp1)`

#### Mission 2

- **Initial state**:
  - `rover1` deployed at `wp2`; `lander1` at `wp2`.
  - `rover2` undeployed; `lander2` placed at `wp5` (to support sample goal at `wp5`).
- **Map**: Undirected graph per **Figure 3** (e.g., `wp1 ↔ wp2`, `wp2 → wp3`, `wp5 ↔ wp6`).
- **Goals**:
  - Data: `img@3`, `scn@4`, `img@2`, `scn@6`
  - Samples: `wp5`, `wp1`

#### Mission 3 (Part 2C Extension)

- **Physical setup**: Identical to Mission 2.
- **Astronauts**:
  - Alice in `docking` of `lander1`
  - Bob in `control` of `lander2`
- **Constraints**:
  - Rover deployment, sample drop, and data transmission now require correct astronaut presence.
- **Goals**: Same as Mission 2 — extension affects **feasibility**, not objectives.

All problem files declare `img`, `scn`, `control`, and `docking` as **objects** (not `:constants`) to ensure compatibility with standard PDDL planners.

---

### 2.3 Planning and Validation

We tested all problem instances using the **LAMA-first** planner via **[editor.planning.domains](https://editor.planning.domains/)** — a compliant, off-the-shelf classical planner that supports `:typing` and STRIPS semantics.

- **Mission 1**: Solved in <1 second. Plan correctly deploys rover, navigates to `wp5`/`wp3`, captures/transmits data, and returns to `wp1` for sample collection.
- **Mission 2**: Planner coordinated both rovers efficiently, generating parallelisable sub-plans.
- **Mission 3**: Initially failed until astronaut movement was included. Final plan interleaves `move_astronaut` with rover tasks to satisfy preconditions (e.g., Bob moves to docking before `rover2` drops sample).

All domains and problems use **only PDDL features covered in the course** (no `:fluents`, `:durative-actions`, or advanced constructs). Full source files (`domain.pddl`, `mission1.pddl`, `mission2.pddl`, `domain-ext`, `mission3`) are included in the submission with inline comments explaining key design choices.

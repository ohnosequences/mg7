# 16S Reference database

The database was built taking the sequences from `nt` that showed similarity with the sequences from the `RDP` database and removing the following sequences:

- They are assigned to taxa with level 3 or higher

- They belong to any of these groups `
    - `root; cellular organisms; Bacteria ; unclassified Bacteria (Taxonomy ID: 2323)`
    - `root; cellular organisms; Bacteria ; environmental samples (Taxonomy ID: 48479)`
    - `root; cellular organisms; Archaea ; unclassified Archaea (Taxonomy ID: 29294)`
    - `root; cellular organisms; Archaea ; environmental samples (Taxonomy ID: 48510)`
    - `root; unclassified sequences (Taxonomy ID: 12908)`
    - `other sequences; artificial sequences ; Synthetic construct (Taxonomy ID: 32630)`

# Strand Command effects

* STEP_OVER:
    * Is equivalent to running a subtree. Cursor moves like running
    * There will be side effects! (leafs
* STEP_INTO:
    * Only changes cursor! (to all children if parallel, to first child if sequential)
    * Not supported for a leaf. -> No side effects
* RESUME:
    * execute up to the end of this strand, starting from the cursor where the strand is. Has to resume sub strands? Should this be global?
* PAUSE: 
    * pause the strand... Shall this be global? 
* SKIP:
    * do not execute the actual block, but move the cursor? To be seeno
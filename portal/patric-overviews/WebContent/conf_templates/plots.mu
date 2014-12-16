<plots>
    {{#tileplots}}
    <plot>
        show = yes
        type = {{type}}
        file = {{file}}
        layers = 1
        margin = 0.02u

        thickness = {{thickness}}
        padding = 1
        orientation = in

        stroke_thickness = 0.1
        stroke_color = {{color}}
        color = {{color}}

        r0 = {{r0}}
        r1 = {{r1}}
        
        url = javascript:linkFeature("[id]")' onmouseover='tooltipFeature(this, "[id]")
    </plot>

    {{/tileplots}}
    {{#nontileplots}}
    <plot>
        show = yes
        type = {{type}}
        file = {{file}}

        r0 = {{r0}}
        r1 = {{r1}}

        min = {{min}}
        max = {{max}}

        orientation = out
        thickness = 1
        color = {{color}}
        {{extendbin}}

        <backgrounds>
            <background>
                color = {{plotbgcolor}}
                y1 = 1r
                y0 = 0r
            </background>
        </backgrounds>

        <axes>
            <axis>
                color = dgrey
                thickness = 0.5
                spacing = 0.25r
            </axis>
        </axes>
    </plot>

    {{/nontileplots}}
</plots>

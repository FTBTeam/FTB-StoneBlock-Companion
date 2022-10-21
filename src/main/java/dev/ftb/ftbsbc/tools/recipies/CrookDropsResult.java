package dev.ftb.ftbsbc.tools.recipies;

import java.util.List;

public record CrookDropsResult(
   List<ItemWithChance> items,
   int max
) {}

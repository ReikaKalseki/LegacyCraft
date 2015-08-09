/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import Reika.DragonAPI.Libraries.Java.ReikaASMHelper;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.Side;

@SortingIndex(1001)
@MCVersion("1.7.10")
public class LegacyASMHandler implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{LegacyTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

	public static class LegacyTransformer implements IClassTransformer {

		private static final HashMap<String, ClassPatch> classes = new HashMap();

		private static final Configuration config = new Configuration(new File("/config/Reika/LegacyCraft/config.cfg"));

		private static boolean getConfig(String sg, boolean def) {
			Property prop = config.get("control setup", sg, def);
			return prop.getBoolean(def);
		}

		private static enum ClassPatch {
			SUGARCANE("net.minecraft.block.BlockReed", "ane"),
			NETHERLAVA("net.minecraft.world.gen.ChunkProviderHell", "aqv"),
			ENDERPORT("net.minecraft.entity.monster.EntityEnderman", "ya"),
			LIGHTMAP("net.minecraft.client.renderer.EntityRenderer", "blt"),
			FOLIAGE("net.minecraft.world.ColorizerFoliage", "agx"),
			ANIMALSPAWN("net.minecraft.world.WorldServer", "mt"),
			PORTAL1("net.minecraft.block.BlockPortal", "amp"),
			PORTAL2("net.minecraft.block.BlockEndPortal", "akt"),
			ICEBLOCK("net.minecraft.block.BlockIce", "alp"),
			LAVAHISS("net.minecraft.block.BlockLiquid", "alw"),
			;

			private final String obfName;
			private final String deobfName;

			private static final ClassPatch[] list = values();

			private ClassPatch(String deobf, String obf) {
				obfName = obf;
				deobfName = deobf;
			}

			private byte[] apply(byte[] data) {
				ClassNode cn = new ClassNode();
				ClassReader classReader = new ClassReader(data);
				classReader.accept(cn, 0);
				switch(this) {
					case SUGARCANE: {
						if (FMLLaunchHandler.side() != Side.CLIENT)
							break;
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149720_d", "colorMultiplier", "(Lnet/minecraft/world/IBlockAccess;III)I");
						AbstractInsnNode start = null;
						AbstractInsnNode ret = null;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain instanceof LineNumberNode) {
								start = ain;
							}
							else if (ain.getOpcode() == Opcodes.IRETURN) {
								ret = ain;
							}
						}
						LabelNode l1 = new LabelNode();
						LabelNode l2 = new LabelNode();
						LabelNode l3 = new LabelNode();

						m.instructions.remove(ret.getNext());
						m.instructions.insert(ret, l3);
						m.instructions.insertBefore(ret, l2);
						m.instructions.insertBefore(ret, new FrameNode(Opcodes.F_SAME1, 0, null, 0, new Object[]{Opcodes.INTEGER}));

						m.instructions.insert(start, new FrameNode(Opcodes.F_SAME, 0, null, 0, null)); //does not use anything but opcode
						m.instructions.insert(start, l1);
						m.instructions.insert(start, new JumpInsnNode(Opcodes.GOTO, l2));
						m.instructions.insert(start, new LdcInsnNode(0xffffff));
						m.instructions.insert(start, new JumpInsnNode(Opcodes.IFEQ, l1));
						m.instructions.insert(start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "Reika/LegacyCraft/LegacyOptions", "getState", "()Z", false));
						m.instructions.insert(start, new FieldInsnNode(Opcodes.GETSTATIC, "Reika/LegacyCraft/LegacyOptions", "SUGARCANE", "LReika/LegacyCraft/LegacyOptions;"));
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case NETHERLAVA: {
						if (!getConfig("Disable Nether Hidden Lava Pockets", true)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_73153_a", "populate", "(Lnet/minecraft/world/chunk/IChunkProvider;II)V");
						Iterator<AbstractInsnNode> it = m.instructions.iterator();
						String gen = FMLForgePlugin.RUNTIME_DEOBF ? "ars" : "net/minecraft/world/gen/feature/WorldGenHellLava";
						while (it.hasNext()) {
							AbstractInsnNode ain = it.next();
							if (ain.getOpcode() == Opcodes.NEW) {
								TypeInsnNode tp = (TypeInsnNode)ain;
								if (tp.desc.equals(gen)) {
									tp.desc = "Reika/LegacyCraft/Overrides/WorldGenCustomNetherLava";
								}
							}
							else if (ain.getOpcode() == Opcodes.INVOKESPECIAL) {
								MethodInsnNode tp = (MethodInsnNode)ain;
								if (tp.owner.equals(gen)) {
									tp.owner = "Reika/LegacyCraft/Overrides/WorldGenCustomNetherLava";
								}
							}
							else if (ain.getOpcode() == Opcodes.INVOKEVIRTUAL) {
								MethodInsnNode tp = (MethodInsnNode)ain;
								if (tp.owner.equals(gen)) {
									tp.owner = "Reika/LegacyCraft/Overrides/WorldGenCustomNetherLava";
								}
							}
						}

						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case ENDERPORT: {
						if (!getConfig("Disable Random Enderman Teleporting in Daylight", true)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_70636_d", "onLivingUpdate", "()V");
						boolean prep = false;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.INVOKEVIRTUAL) {
								MethodInsnNode min = (MethodInsnNode)ain;
								String func = FMLForgePlugin.RUNTIME_DEOBF ? "func_70013_c" : "getBrightness";
								if (func.equals(min.name)) {
									prep = true;
								}
							}
							else if (prep && ain.getOpcode() == Opcodes.LDC) {
								LdcInsnNode ldc = (LdcInsnNode)ain;
								ldc.cst = Float.MAX_VALUE;
								ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
								break;
							}
						}
						break;
					}
					case LIGHTMAP: {
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_78472_g", "updateLightmap", "(F)V");
						AbstractInsnNode loc = null;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.IASTORE) {
								loc = ain;
							}
						}
						if (loc != null) {
							m.instructions.insert(loc, new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyCraft", "adjustLightMap", "()V", false));
							ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						}
						else {
							ReikaASMHelper.log("Could not apply "+this+" ASM handler; reference IASTORE not found!");
						}
						break;
					}
					case FOLIAGE: {
						if (!getConfig("Alpha Grass and Leaf Color", false)) { //for some reason not working
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_77469_b", "getFoliageColorBirch", "()I");
						AbstractInsnNode loc = null;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.LDC) {
								loc = ain;
								break;
							}
						}

						m.instructions.insert(loc, new LdcInsnNode(0xffffff));
						m.instructions.remove(loc);

						m = ReikaASMHelper.getMethodByName(cn, "func_77466_a", "getFoliageColorPine", "()I");

						loc = null;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.LDC) {
								loc = ain;
								break;
							}
						}
						m.instructions.insert(loc, new LdcInsnNode(0xffffff));
						m.instructions.remove(loc);

						m = ReikaASMHelper.getMethodByName(cn, "func_77468_c", "getFoliageColorBasic", "()I");

						loc = null;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.LDC) {
								loc = ain;
								break;
							}
							m.instructions.insert(loc, new LdcInsnNode(0xffffff));
							m.instructions.remove(loc);
						}
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
					}
					break;
					case ANIMALSPAWN: {
						if (!getConfig("Pre Adventure Update Animal Spawning", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_72835_b", "tick", "()V");
						AbstractInsnNode loc = null;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.LDC) {
								LdcInsnNode ldc = (LdcInsnNode)ain;
								if (ldc.cst instanceof Integer && ((Integer)ldc.cst).intValue() == 400) {
									loc = ain;
									break;
								}
							}
						}
						m.instructions.insert(loc, new LdcInsnNode(40));
						m.instructions.remove(loc);
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case PORTAL1: {
						if (!getConfig("Disable Entities Travelling Through Portals", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149670_a", "onEntityCollidedWithBlock", "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)V");
						LabelNode l1 = new LabelNode();
						LabelNode l2 = new LabelNode();
						InsnList insert = new InsnList();
						insert.add(new VarInsnNode(Opcodes.ALOAD, 5));
						insert.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/entity/player/EntityPlayer"));
						insert.add(new JumpInsnNode(Opcodes.IFNE, l1));
						insert.add(l2);
						insert.add(new InsnNode(Opcodes.RETURN));
						insert.add(l1);
						m.instructions.insert(insert);
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case PORTAL2: {
						if (!getConfig("Disable Entities Travelling Through Portals", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149670_a", "onEntityCollidedWithBlock", "(Lnet/minecraft/world/World;IIILnet/minecraft/entity/Entity;)V");
						LabelNode l1 = new LabelNode();
						LabelNode l2 = new LabelNode();
						InsnList insert = new InsnList();
						insert.add(new VarInsnNode(Opcodes.ALOAD, 5));
						insert.add(new TypeInsnNode(Opcodes.INSTANCEOF, "net/minecraft/entity/player/EntityPlayer"));
						insert.add(new JumpInsnNode(Opcodes.IFNE, l1));
						insert.add(l2);
						insert.add(new InsnNode(Opcodes.RETURN));
						insert.add(l1);
						m.instructions.insert(insert);
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case ICEBLOCK: {
						if (!getConfig("Enable Ice to Water in Nether", true)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						String hell = FMLForgePlugin.RUNTIME_DEOBF ? "field_76575_d" : "isHellWorld";
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149636_a", "harvestBlock", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;IIII)V");
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.GETFIELD) {
								FieldInsnNode fin = (FieldInsnNode)ain;
								if (fin.name.equals(hell)) {
									boolean last = false;
									while (!last) {
										AbstractInsnNode next = fin.getNext();
										last = next.getOpcode() == Opcodes.RETURN;
										m.instructions.remove(next);
									}
									m.instructions.insert(fin, new InsnNode(Opcodes.POP));
									ReikaASMHelper.log("Successfully applied "+this+" ASM handler 1!");
									break;
								}
							}
						}

						m = ReikaASMHelper.getMethodByName(cn, "func_149674_a", "updateTick", "(Lnet/minecraft/world/World;IIILjava/util/Random;)V");
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.GETFIELD) {
								FieldInsnNode fin = (FieldInsnNode)ain;
								if (fin.name.equals(hell)) {
									boolean last = false;
									while (!last) {
										AbstractInsnNode next = fin.getNext();
										last = next.getOpcode() == Opcodes.RETURN;
										m.instructions.remove(next);
									}
									m.instructions.insert(fin, new InsnNode(Opcodes.POP));
									ReikaASMHelper.log("Successfully applied "+this+" ASM handler 2!");
									break;
								}
							}
						}
						break;
					}
					case LAVAHISS: {
						if (!getConfig("Lava Movement Hiss", true)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149799_m", "func_149799_m", "(Lnet/minecraft/world/World;III)V");
						m.instructions.clear();
						m.instructions.add(new InsnNode(Opcodes.RETURN));
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
				}
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS/* | ClassWriter.COMPUTE_FRAMES*/);
				cn.accept(writer);
				return writer.toByteArray();
			}
		}

		@Override
		public byte[] transform(String className, String className2, byte[] opcodes) {
			if (!classes.isEmpty()) {
				ClassPatch p = classes.get(className);
				if (p != null) {
					ReikaASMHelper.activeMod = "LegacyCraft";
					ReikaASMHelper.log("Patching class "+p.deobfName);
					opcodes = p.apply(opcodes);
					classes.remove(className); //for maximizing performance
					ReikaASMHelper.activeMod = null;
				}
			}
			return opcodes;
		}

		static {
			for (int i = 0; i < ClassPatch.list.length; i++) {
				ClassPatch p = ClassPatch.list[i];
				String s = !FMLForgePlugin.RUNTIME_DEOBF ? p.deobfName : p.obfName;
				classes.put(s, p);
			}
		}

	}

}

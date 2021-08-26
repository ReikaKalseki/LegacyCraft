/*******************************************************************************
 * @author Reika Kalseki
 *
 * Copyright 2017
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.LegacyCraft;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import Reika.DragonAPI.Exception.ASMException.NoSuchASMMethodException;
import Reika.DragonAPI.Instantiable.Data.Maps.MultiMap;
import Reika.DragonAPI.Libraries.Java.ReikaASMHelper;
import Reika.DragonAPI.Libraries.Java.ReikaASMHelper.PrimitiveType;

import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.Side;

@SortingIndex(Integer.MAX_VALUE-1)
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

		private static final MultiMap<String, ClassPatch> classes = new MultiMap().setNullEmpty();

		private static final Configuration config = new Configuration(new File("/config/Reika/LegacyCraft/config.cfg"));

		private static boolean getConfig(String sg, boolean def) {
			Property prop = config.get("control setup", sg, def);
			return prop.getBoolean(def);
		}

		private static enum ClassPatch {
			SUGARCANE("net.minecraft.block.BlockReed", "ane"),
			NETHERLAVA("net.minecraft.world.gen.ChunkProviderHell", "aqv"),
			ENDERPORT("net.minecraft.entity.monster.EntityEnderman", "ya"),
			FOLIAGE("net.minecraft.world.ColorizerFoliage", "agx"),
			ANIMALSPAWN("net.minecraft.world.WorldServer", "mt"),
			PORTAL1("net.minecraft.block.BlockPortal", "amp"),
			PORTAL2("net.minecraft.block.BlockEndPortal", "akt"),
			ICEBLOCK("net.minecraft.block.BlockIce", "alp"),
			WATERPATCH("net.minecraft.block.Block", "aji"),
			LAVAHISS("net.minecraft.block.BlockLiquid", "alw"),
			FLINTSOUND("net.minecraft.item.ItemFlintAndSteel", "acw"),
			CREEPERFALL("net.minecraft.entity.monster.EntityCreeper", "xz"),
			CREEPERAI("net.minecraft.entity.monster.EntityCreeper", "xz"),
			SKELLYAI("net.minecraft.entity.monster.EntitySkeleton", "yl"),
			ZOMBIEAI("net.minecraft.entity.monster.EntityZombie", "yq"),
			CREEPERENCHANT("net.minecraft.entity.monster.EntityCreeper", "xz"),
			SKELLYENCHANT("net.minecraft.entity.monster.EntitySkeleton", "yl"),
			ZOMBIEENCHANT("net.minecraft.entity.monster.EntityZombie", "yq"),
			SPIDERENCHANT("net.minecraft.entity.monster.EntitySpider", "yn"),
			EQUIPDMG("net.minecraft.entity.EntityLiving", "sw"),
			ZOMBIEHOOKS("net.minecraft.entity.monster.EntityZombie", "yq"),
			CREEPERSPAWN("net.minecraft.entity.monster.EntityCreeper", "xz"),
			SKELLYSPAWN("net.minecraft.entity.monster.EntitySkeleton", "yl"),
			ZOMBIESPAWN("net.minecraft.entity.monster.EntityZombie", "yq"),
			SPIDERSPAWN("net.minecraft.entity.monster.EntitySpider", "yn"),
			CREEPERATTR("net.minecraft.entity.monster.EntityCreeper", "xz"),
			SKELLYATTR("net.minecraft.entity.monster.EntitySkeleton", "yl"),
			ZOMBIEATTR("net.minecraft.entity.monster.EntityZombie", "yq"),
			SPIDERATTR("net.minecraft.entity.monster.EntitySpider", "yn"),
			ZOMBIETOOLS("net.minecraft.entity.monster.EntityZombie", "yq"),
			MOBARMOR("net.minecraft.entity.monster.EntityMob", "yg"),
			ZOMBIEFIRE("net.minecraft.entity.monster.EntityZombie", "yq"),
			SKELLYRATE("net.minecraft.entity.monster.EntitySkeleton", "yl"),
			SKELLYFIRE("net.minecraft.entity.monster.EntitySkeleton", "yl"),
			SILENTVILLAGERS("net.minecraft.entity.passive.EntityVillager", "yv"),
			ENDERSOUNDS("net.minecraft.entity.monster.EntityEnderman", "ya"),
			;

			private final String obfName;
			private final String deobfName;

			private static final ClassPatch[] list = values();

			private ClassPatch(String deobf, String obf) {
				obfName = obf;
				deobfName = deobf;
			}

			private static void redirectInstanceFunctionCall(ClassNode cn, MethodInsnNode min, String name) {
				min.owner = "Reika/LegacyCraft/LegacyASMHooks";
				ReikaASMHelper.addLeadingArgument(min, ReikaASMHelper.convertClassName(cn, true));
				min.setOpcode(Opcodes.INVOKESTATIC);
			}

			private static void redirectInstanceFunction(ClassNode cn, MethodNode m, String name) {
				/*
				m.instructions.clear();
				ArrayList<String> li = ReikaASMHelper.parseMethodSignature(m);
				if ((m.access & Modifier.STATIC) == 0) {
					li.add(0, ReikaASMHelper.convertClassName(cn, true));
					m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				}
				for (int i = 1; i < li.size()-1; i++) {
					String arg = li.get(i);
					PrimitiveType p = PrimitiveType.getFromSig(arg);
					m.instructions.add(new VarInsnNode(p.loadCode, i));
				}
				String sig = ReikaASMHelper.compileSignature(li);
				MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyASMHooks", name, sig, false);
				m.instructions.add(call);
				PrimitiveType pret = PrimitiveType.getFromSig(li.get(li.size()-1));
				InsnNode ret = new InsnNode(pret.returnCode);
				m.instructions.add(ret);
				 */
				MethodInsnNode call = ReikaASMHelper.rerouteMethod(cn, m, "Reika/LegacyCraft/LegacyASMHooks", name);
				ReikaASMHelper.log(ReikaASMHelper.clearString(m.instructions));
				ReikaASMHelper.log("Constructed redirect in "+ReikaASMHelper.clearString(m)+" to "+ReikaASMHelper.clearString(call)+".");
			}

			private static void redirectInstanceFunctionWithReturnHook(ClassNode cn, MethodNode m, String name) {
				InsnList calls = new InsnList();
				ArrayList<String> li = ReikaASMHelper.parseMethodSignature(m);
				if ((m.access & Modifier.STATIC) == 0) {
					li.add(0, ReikaASMHelper.convertClassName(cn, true));
					calls.add(new VarInsnNode(Opcodes.ALOAD, 0));
				}
				for (int i = 1; i < li.size()-1; i++) {
					String arg = li.get(i);
					PrimitiveType p = PrimitiveType.getFromSig(arg);
					calls.add(new VarInsnNode(p.loadCode, i));
				}
				li.add(0, li.get(li.size()-1));
				String sig = ReikaASMHelper.compileSignature(li);
				MethodInsnNode call = new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyASMHooks", name, sig, false);
				calls.add(call);
				for (int i = m.instructions.size()-1; i >= 0; i--) {
					AbstractInsnNode ain = m.instructions.get(i);
					if (ReikaASMHelper.isReturn(ain)) {
						m.instructions.insertBefore(ain, ReikaASMHelper.copyInsnList(calls));
					}
				}
				ReikaASMHelper.log(ReikaASMHelper.clearString(m.instructions));
				ReikaASMHelper.log("Constructed return-preserving redirect in "+ReikaASMHelper.clearString(m)+" to "+ReikaASMHelper.clearString(call)+".");
			}

			private static MethodNode getOrCreateMethod(ClassNode cn, String obf, String deobf, String desc) {
				return getOrCreateMethod(cn, obf, deobf, desc, true);
			}

			private static MethodNode getOrCreateMethod(ClassNode cn, String obf, String deobf, String desc, boolean clear) {
				MethodNode m = null;
				try {
					m = ReikaASMHelper.getMethodByName(cn, obf, deobf, desc);
				}
				catch (NoSuchASMMethodException e) {
					m = ReikaASMHelper.addMethod(cn, new InsnList(), FMLForgePlugin.RUNTIME_DEOBF ? obf : deobf, desc, Modifier.PUBLIC);
				}
				if (clear)
					m.instructions.clear();
				return m;
			}

			private static void patchMoveSpeed(ClassNode cn) {
				MethodNode m = getOrCreateMethod(cn, "func_70689_ay", "getAIMoveSpeed", "()F");
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, cn.superName, "getAIMoveSpeed", "()F", false));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyASMHooks", "getAIMoveSpeed", "(F)F", false));
				m.instructions.add(new InsnNode(Opcodes.FRETURN));
			}

			private static void patchAI(ClassNode cn) {
				MethodNode m = getOrCreateMethod(cn, "func_70650_aV", "isAIEnabled", "()Z");
				m.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "Reika/LegacyCraft/LegacyOptions", "NEWAI", "LReika/LegacyCraft/LegacyOptions;"));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "Reika/LegacyCraft/LegacyOptions", "getState", "()Z", false));
				m.instructions.add(new InsnNode(Opcodes.IRETURN));
			}

			private static void patchToolEnchant(ClassNode cn) {
				MethodNode m = getOrCreateMethod(cn, "func_82162_bC", "enchantEquipment", "()V");
				LabelNode lb = new LabelNode();
				m.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "Reika/LegacyCraft/LegacyOptions", "HELDENCHANT", "LReika/LegacyCraft/LegacyOptions;"));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "Reika/LegacyCraft/LegacyOptions", "getState", "()Z", false));
				m.instructions.add(new JumpInsnNode(Opcodes.IFEQ, lb));
				m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
				m.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, cn.superName, "enchantEquipment", "()V", false));
				m.instructions.add(lb);
				//m.instructions.add(new InsnNode(Opcodes.FRAME SAME));
				m.instructions.add(new InsnNode(Opcodes.RETURN));
			}

			private static void addEggSpawnHook(ClassNode cn) {
				MethodNode m = getOrCreateMethod(cn, "func_110161_a", "onSpawnWithEgg", "(Lnet/minecraft/entity/IEntityLivingData;)Lnet/minecraft/entity/IEntityLivingData;", false);
				InsnList li = new InsnList();
				li.add(new VarInsnNode(Opcodes.ALOAD, 0));
				li.add(new VarInsnNode(Opcodes.ALOAD, 1));
				li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyASMHooks", "onEntitySpawn", "(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/entity/IEntityLivingData;)Lnet/minecraft/entity/IEntityLivingData;", false));
				if (m.instructions.size() == 0) {
					m.instructions.add(li);
					m.instructions.add(new InsnNode(Opcodes.ARETURN));
				}
				else {
					for (int i = m.instructions.size()-1; i >= 0; i--) {
						AbstractInsnNode ain = m.instructions.get(i);
						if (ain.getOpcode() == Opcodes.ARETURN) {
							m.instructions.insertBefore(ain, ReikaASMHelper.copyInsnList(li));
						}
					}
				}
			}

			private static void patchEntityAttr(ClassNode cn) {
				MethodNode m = getOrCreateMethod(cn, "func_110147_ax", "applyEntityAttributes", "()V", false);
				InsnList li = new InsnList();
				li.add(new VarInsnNode(Opcodes.ALOAD, 0));
				li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyASMHooks", "applyEntityAttributes", "(Lnet/minecraft/entity/EntityLiving;)V", false));
				if (m.instructions.size() == 0) {
					m.instructions.add(li);
					m.instructions.add(new InsnNode(Opcodes.RETURN));
				}
				else {
					for (int i = m.instructions.size()-1; i >= 0; i--) {
						AbstractInsnNode ain = m.instructions.get(i);
						if (ain.getOpcode() == Opcodes.RETURN) {
							m.instructions.insertBefore(ain, ReikaASMHelper.copyInsnList(li));
						}
					}
				}
			}

			private byte[] apply(byte[] data) {
				ClassNode cn = new ClassNode();
				ClassReader classReader = new ClassReader(data);
				classReader.accept(cn, 0);
				int flags = ClassWriter.COMPUTE_MAXS/* | ClassWriter.COMPUTE_FRAMES*/;
				switch(this) {
					case SUGARCANE: {
						if (FMLLaunchHandler.side() != Side.CLIENT)
							break;
						if (!getConfig("Disable Biome Colors on Sugarcane", true)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149720_d", "colorMultiplier", "(Lnet/minecraft/world/IBlockAccess;III)I");
						m.instructions.clear();
						m.instructions.add(new LdcInsnNode(0xffffff));
						m.instructions.add(new InsnNode(Opcodes.IRETURN));
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
					case WATERPATCH: {
						if (!getConfig("Enable Ice to Water in Nether", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149671_p", "registerBlocks", "()V");

						AbstractInsnNode loc = ReikaASMHelper.getFirstInsnAfter(m.instructions, 0, Opcodes.BIPUSH, 8);
						loc = ReikaASMHelper.getFirstOpcodeAfter(m.instructions, m.instructions.indexOf(loc), Opcodes.NEW);
						TypeInsnNode type = (TypeInsnNode)loc;
						int idx = m.instructions.indexOf(type);
						type.desc = "net/minecraft/block/BlockDynamicLiquid";
						MethodInsnNode min = (MethodInsnNode)ReikaASMHelper.getFirstOpcodeAfter(m.instructions, idx, Opcodes.INVOKESPECIAL);
						min.owner = type.desc;

						type = (TypeInsnNode)ReikaASMHelper.getFirstOpcodeAfter(m.instructions, idx+1, Opcodes.NEW);
						idx = m.instructions.indexOf(type);
						type.desc = "net/minecraft/block/BlockStaticLiquid";
						min = (MethodInsnNode)ReikaASMHelper.getFirstOpcodeAfter(m.instructions, idx, Opcodes.INVOKESPECIAL);
						min.owner = type.desc;

						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
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
					case FLINTSOUND: {
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_77648_a", "onItemUse", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIIIFFF)Z");
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.LDC) {
								LdcInsnNode ldc = (LdcInsnNode)ain;
								if ("fire.ignite".equals(ldc.cst)) {
									ReikaASMHelper.replaceInstruction(m.instructions, ain, new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyASMHooks", "getFlintAndSteelSound", "()Ljava/lang/String;", false));
								}
							}
						}
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case CREEPERFALL: {
						if (getConfig("Creepers Explode on Fall", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						if (ReikaASMHelper.removeMethod(cn, FMLForgePlugin.RUNTIME_DEOBF ? "func_70069_a" : "fall", "(F)V") == null)
							throw new NoSuchASMMethodException(cn, "fall", "(F)V");
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case CREEPERAI:
					case SKELLYAI:
					case ZOMBIEAI: {
						patchAI(cn);
						patchMoveSpeed(cn);
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case CREEPERENCHANT:
					case SKELLYENCHANT:
					case ZOMBIEENCHANT:
					case SPIDERENCHANT: {
						this.patchToolEnchant(cn);
						flags |= ClassWriter.COMPUTE_FRAMES;
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case EQUIPDMG: {
						if (getConfig("Damaged Mob Weapon Drops", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_82160_b", "dropEquipment", "(ZI)V");
						String func = FMLForgePlugin.RUNTIME_DEOBF ? "func_77964_b" : "setItemDamage";
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.INVOKEVIRTUAL) {
								MethodInsnNode min = (MethodInsnNode)ain;
								if (func.equals(min.name)) {
									m.instructions.insertBefore(min, new InsnNode(Opcodes.POP));
									m.instructions.insertBefore(min, new InsnNode(Opcodes.ICONST_0));
								}
							}
						}
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case ZOMBIEHOOKS: {
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_110161_a", "onSpawnWithEgg", "(Lnet/minecraft/entity/IEntityLivingData;)Lnet/minecraft/entity/IEntityLivingData;");
						FieldInsnNode fin = ReikaASMHelper.getFirstFieldCallByName(cn, m, "field_142046_b");
						int var = ((VarInsnNode)fin.getPrevious()).var;
						VarInsnNode vin = (VarInsnNode)ReikaASMHelper.getLastInsnBefore(m.instructions, m.instructions.indexOf(fin), Opcodes.ASTORE, var);
						m.instructions.insertBefore(vin, new VarInsnNode(Opcodes.ALOAD, 0));
						m.instructions.insertBefore(vin, new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/LegacyASMHooks", "interceptZombieData", "(Lnet/minecraft/entity/monster/EntityZombie$GroupData;Lnet/minecraft/entity/monster/EntityZombie;)Lnet/minecraft/entity/monster/EntityZombie$GroupData;", false));
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case CREEPERSPAWN:
					case SKELLYSPAWN:
					case ZOMBIESPAWN:
					case SPIDERSPAWN: {
						this.addEggSpawnHook(cn);
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case CREEPERATTR:
					case SKELLYATTR:
					case ZOMBIEATTR:
					case SPIDERATTR: {
						this.patchEntityAttr(cn);
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case ZOMBIETOOLS: {
						if (getConfig("Zombies Can Spawn With Tools", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						if (ReikaASMHelper.removeMethod(cn, FMLForgePlugin.RUNTIME_DEOBF ? "func_82164_bB" : "addRandomArmor", "()V") == null)
							throw new NoSuchASMMethodException(cn, "addRandomArmor", "()V");
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case MOBARMOR: {
						if (getConfig("Mobs Can Spawn With Armor", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = getOrCreateMethod(cn, "func_82164_bB", "addRandomArmor", "()V");
						m.instructions.add(new InsnNode(Opcodes.RETURN));
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case ZOMBIEFIRE: {
						if (getConfig("Zombies Attack with Fire", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_70652_k", "attackEntityAsMob", "(Lnet/minecraft/entity/Entity;)Z");
						MethodInsnNode min = ReikaASMHelper.getFirstMethodCallByName(cn, m, FMLForgePlugin.RUNTIME_DEOBF ? "func_70027_ad" : "isBurning");
						AbstractInsnNode prev = min.getPrevious();
						ReikaASMHelper.replaceInstruction(m.instructions, min, new InsnNode(Opcodes.ICONST_0));
						m.instructions.remove(prev);
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case SKELLYRATE: {
						if (!getConfig("Old Skeleton Fire Rate", true)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "<init>", "(Lnet/minecraft/world/World;)V");
						IntInsnNode iin = (IntInsnNode)ReikaASMHelper.getFirstInsnAfter(m.instructions, 0, Opcodes.BIPUSH, 60);
						iin.setOpcode(Opcodes.SIPUSH);
						iin.operand = 300;
						ReikaASMHelper.replaceInstruction(m.instructions, iin.getPrevious(), new InsnNode(Opcodes.ICONST_0));
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case SKELLYFIRE: {
						if (getConfig("Allow Skeleton Flaming Arrows", false)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_82196_d", "attackEntityWithRangedAttack", "(Lnet/minecraft/entity/EntityLivingBase;F)V");
						FieldInsnNode fin = ReikaASMHelper.getFirstFieldCallByName(cn, m, FMLForgePlugin.RUNTIME_DEOBF ? "field_77343_v" : "flame");
						JumpInsnNode jin = (JumpInsnNode)ReikaASMHelper.getFirstOpcodeAfter(m.instructions, m.instructions.indexOf(fin), Opcodes.IFGT);
						jin.setOpcode(Opcodes.IF_ICMPGT);
						m.instructions.insertBefore(jin, new IntInsnNode(Opcodes.BIPUSH, 100));
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case SILENTVILLAGERS: {
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_70639_aQ", "getLivingSound", "()Ljava/lang/String;");
						redirectInstanceFunctionWithReturnHook(cn, m, "getVillagerSound");

						m = ReikaASMHelper.getMethodByName(cn, "func_70621_aR", "getHurtSound", "()Ljava/lang/String;");
						redirectInstanceFunctionWithReturnHook(cn, m, "getVillagerSound");

						m = ReikaASMHelper.getMethodByName(cn, "func_70673_aS", "getDeathSound", "()Ljava/lang/String;");
						redirectInstanceFunctionWithReturnHook(cn, m, "getVillagerSound");
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
					case ENDERSOUNDS: {
						if (getConfig("New Angry Enderman Sounds", true)) {
							ReikaASMHelper.log("Not applying "+this+" ASM handler; disabled in config.");
							return data;
						}
						MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_70823_r", "isScreaming", "()Z");
						m.instructions.clear();
						m.instructions.add(new InsnNode(Opcodes.ICONST_0));
						m.instructions.add(new InsnNode(Opcodes.RETURN));

						m = ReikaASMHelper.getMethodByName(cn, "func_70782_k", "findPlayerToAttack", "()Lnet/minecraft/entity/Entity;");
						LdcInsnNode ldc = (LdcInsnNode)ReikaASMHelper.getFirstInsnAfter(m.instructions, 0, Opcodes.LDC, "mob.endermen.stare");
						ReikaASMHelper.replaceInstruction(m.instructions, ldc.getNext(), new InsnNode(Opcodes.FCONST_0));
						ReikaASMHelper.log("Successfully applied "+this+" ASM handler!");
						break;
					}
				}
				ClassWriter writer = new ClassWriter(flags);
				cn.accept(writer);
				return writer.toByteArray();
			}
		}

		@Override
		public byte[] transform(String className, String className2, byte[] opcodes) {
			if (!classes.isEmpty()) {
				Collection<ClassPatch> c = classes.get(className);
				if (c != null) {
					ReikaASMHelper.activeMod = "LegacyCraft";
					ReikaASMHelper.log("Patching class "+c.iterator().next().deobfName);
					for (ClassPatch p : c) {
						opcodes = p.apply(opcodes);
					}
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
				classes.addValue(s, p);
			}
		}

	}

}

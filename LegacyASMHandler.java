package Reika.LegacyCraft;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.config.Configuration;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import Reika.DragonAPI.Libraries.Java.ReikaASMHelper;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;

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

		private static boolean getConfig(String sg) {
			return config.get("control setup", sg, true).getBoolean(true);
		}

		private static enum ClassPatch {
			SUGARCANE("net.minecraft.block.BlockReed", "ane"),
			NETHERLAVA("net.minecraft.world.gen.ChunkProviderHell", "aqv"),
			ENDERPORT("net.minecraft.entity.monster.EntityEnderman", "bhk");

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
					MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_149720_d", "colorMultiplier", "(Lnet/minecraft/world/IBlockAccess;III)I");
					if (m == null) {
						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Could not find method for "+this+" ASM handler!");
					}
					else {
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
						m.instructions.insert(start, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "Reika/LegacyCraft/LegacyOptions", "getState", "()Z"));
						m.instructions.insert(start, new FieldInsnNode(Opcodes.GETSTATIC, "Reika/LegacyCraft/LegacyOptions", "SUGARCANE", "LReika/LegacyCraft/LegacyOptions;"));
						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Successfully applied "+this+" ASM handler!");
					}
					break;
				}
				case NETHERLAVA: {
					if (!getConfig("Disable Nether Hidden Lava Pockets")) {
						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Not applying "+this+" ASM handler; disabled in config.");
						return data;
					}
					//MethodNode m = ReikaASMHelper.getMethodByName(cn, "", "populate", "(Lnet/minecraft/world/chunk/IChunkProvider;II)V");
					MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_147419_a", "func_147419_a", "(II[Lnet/minecraft/block/Block;)V");
					if (m == null) {
						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Could not find method for "+this+" ASM handler!");
					}
					else {
						//ReikaASMHelper.clearMethodBody(m);
						//m.instructions.add(new InsnNode(Opcodes.ICONST_0));
						//m.instructions.add(new InsnNode(Opcodes.IRETURN));

						/*
						int count = 0;
						boolean prep = false;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.GETSTATIC) {
								FieldInsnNode min = (FieldInsnNode)ain;
								if ("QUARTZ".equals(min.name)) {
									prep = true;
								}
							}
							else if (prep && ain.getOpcode() == Opcodes.BIPUSH) {
								if (count == 5) {
									IntInsnNode ldc = (IntInsnNode)ain;
									ldc.operand = Integer.MIN_VALUE;
									break;
								}
								else {
									count++;
								}
							}
						}
						 */

						//ReikaASMHelper.removeCodeLine(m, 237);

						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Successfully applied "+this+" ASM handler!");
					}
					break;
				}
				case ENDERPORT: {
					if (!getConfig("Disable Random Enderman Teleporting in Daylight")) {
						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Not applying "+this+" ASM handler; disabled in config.");
						return data;
					}
					MethodNode m = ReikaASMHelper.getMethodByName(cn, "func_70636_d", "onLivingUpdate", "()V");
					if (m == null) {
						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Could not find method for "+this+" ASM handler!");
					}
					else {/*
						for (int i = 214; i <= 223; i++) {
							ReikaASMHelper.removeCodeLine(m, i);
						}*/
						boolean prep = false;
						for (int i = 0; i < m.instructions.size(); i++) {
							AbstractInsnNode ain = m.instructions.get(i);
							if (ain.getOpcode() == Opcodes.INVOKEVIRTUAL) {
								MethodInsnNode min = (MethodInsnNode)ain;
								String func = FMLForgePlugin.RUNTIME_DEOBF ? "" : "getBrightness";
								if (func.equals(min.name)) {
									prep = true;
								}
							}
							else if (prep && ain.getOpcode() == Opcodes.LDC) {
								LdcInsnNode ldc = (LdcInsnNode)ain;
								ldc.cst = Float.MAX_VALUE;
								break;
							}
						}
						ReikaJavaLibrary.pConsole("LEGACYCRAFT: Successfully applied "+this+" ASM handler!");
					}
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
					ReikaJavaLibrary.pConsole("LEGACYCRAFT: Patching class "+className);
					opcodes = p.apply(opcodes);
					classes.remove(className); //for maximizing performance
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

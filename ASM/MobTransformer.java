package Reika.LegacyCraft.ASM;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

import Reika.DragonAPI.Libraries.Java.ReikaASMHelper;


public class MobTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] data) {
		ReikaASMHelper.activeMod = "LegacyCraft";
		//ReikaASMHelper.log("Applying mob patches to "+name+"/"+transformedName);
		for (Patches p : Patches.list) {
			try {
				data = this.write(p, data);
			}
			catch (Throwable e) {
				ClassNode cn = new ClassNode();
				ClassReader classReader = new ClassReader(data);
				classReader.accept(cn, 0);
				p.apply(cn, ClassWriter.COMPUTE_MAXS);
				ReikaASMHelper.logError("Unable to patch mob class '"+cn.name+"' ^ '"+cn.superName+"': "+e.toString());
				/*
				for (MethodNode m : cn.methods) {
					ReikaASMHelper.log("====  METHOD "+m.name+" "+m.desc+"  ====");
					ReikaASMHelper.log(ReikaASMHelper.clearString(m.instructions));
					ReikaASMHelper.log("=====================");
				}*/
				ReikaASMHelper.writeClassFile(cn, "LgCASMOutput");
				e.printStackTrace();
			}
		}
		ReikaASMHelper.activeMod = null;
		return data;
	}

	private byte[] write(Patches p, byte[] data) {
		ClassNode cn = new ClassNode();
		ClassReader classReader = new ClassReader(data);
		classReader.accept(cn, 0);
		if (!p.appliesTo(cn)) {
			return data;
		}
		ReikaASMHelper.log("Applying "+p+" ASM handler to "+cn.name);
		int flags = ClassWriter.COMPUTE_MAXS/* | ClassWriter.COMPUTE_FRAMES*/;
		flags = p.apply(cn, flags);
		ClassWriter writer = new ClassWriter(flags);
		cn.accept(writer);
		ReikaASMHelper.log("Successfully applied "+p+" ASM handler to "+cn.name+"!");
		return writer.toByteArray();
	}

	@Deprecated
	private static void patchAI(ClassNode cn) {
		MethodNode m = LegacyASMHandler.getOrCreateMethod(cn, "func_70650_aV", "isAIEnabled", "()Z");
		m.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "Reika/LegacyCraft/LegacyOptions", "NEWAI", "LReika/LegacyCraft/LegacyOptions;"));
		m.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "Reika/LegacyCraft/LegacyOptions", "getState", "()Z", false));
		m.instructions.add(new InsnNode(Opcodes.IRETURN));
	}

	@Deprecated
	private static void patchMoveSpeed(ClassNode cn) {
		MethodNode m = LegacyASMHandler.getOrCreateMethod(cn, "func_70689_ay", "getAIMoveSpeed", "()F");
		m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		m.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, cn.superName, "getAIMoveSpeed", "()F", false));
		m.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/ASM/LegacyASMHooks", "getAIMoveSpeed", "(F)F", false));
		m.instructions.add(new InsnNode(Opcodes.FRETURN));
	}

	private static void addEggSpawnHook(ClassNode cn) {
		MethodNode m = LegacyASMHandler.getOrCreateMethod(cn, "func_110161_a", "onSpawnWithEgg", "(Lnet/minecraft/entity/IEntityLivingData;)Lnet/minecraft/entity/IEntityLivingData;", false);
		InsnList li = new InsnList();
		li.add(new VarInsnNode(Opcodes.ALOAD, 0));
		li.add(new VarInsnNode(Opcodes.ALOAD, 1));
		li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/ASM/LegacyASMHooks", "onEntitySpawn", "(Lnet/minecraft/entity/EntityLiving;Lnet/minecraft/entity/IEntityLivingData;)Lnet/minecraft/entity/IEntityLivingData;", false));
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
		MethodNode m = LegacyASMHandler.getOrCreateMethod(cn, "func_110147_ax", "applyEntityAttributes", "()V", false);
		InsnList li = new InsnList();
		li.add(new VarInsnNode(Opcodes.ALOAD, 0));
		li.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "Reika/LegacyCraft/ASM/LegacyASMHooks", "applyEntityAttributes", "(Lnet/minecraft/entity/EntityLiving;)V", false));
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

	private static void patchToolEnchant(ClassNode cn) {
		MethodNode m = LegacyASMHandler.getOrCreateMethod(cn, "func_82162_bC", "enchantEquipment", "()V");
		LabelNode lb = new LabelNode();
		m.instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, "Reika/LegacyCraft/LegacyOptions", "HELDENCHANT", "LReika/LegacyCraft/LegacyOptions;"));
		m.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "Reika/LegacyCraft/LegacyOptions", "getState", "()Z", false));
		m.instructions.add(new JumpInsnNode(Opcodes.IFEQ, lb));
		m.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		m.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, cn.superName, m.name, m.desc, false));
		m.instructions.add(lb);
		//m.instructions.add(new InsnNode(Opcodes.FRAME SAME));
		m.instructions.add(new InsnNode(Opcodes.RETURN));
	}

	private static boolean extendsMob(ClassNode cn) {
		return cn.superName != null && (cn.superName.equals("net/minecraft/entity/monster/EntityMob") || cn.superName.equals("yg"));
	}

	private static enum Patches {
		SPAWN,
		ATTR,
		ENCHANT,
		//AI,
		;

		private static final Patches[] list = values();

		private int apply(ClassNode cn, int flags) {
			switch(this) {
				case ATTR:
					patchEntityAttr(cn);
					break;
				case ENCHANT:
					patchToolEnchant(cn);
					flags |= ClassWriter.COMPUTE_FRAMES;
					break;
				case SPAWN:
					addEggSpawnHook(cn);
					break;/*
				case AI:
					patchAI(cn);
					patchMoveSpeed(cn);
					break;*/
			}
			return flags;
		}

		public boolean appliesTo(ClassNode cn) {
			switch(this) {
				//case AI:
				//	return extendsMob(cn) && !cn.name.contains("Spider");
				default:
					return extendsMob(cn) && !cn.name.contains("Satisforestry");
			}
		}
	}

}

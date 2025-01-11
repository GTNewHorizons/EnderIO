package crazypants.enderio.machine.ranged;

import akka.io.Tcp;
import com.enderio.core.common.vecmath.Vector2d;
import com.enderio.core.common.vecmath.Vector2f;
import com.enderio.core.common.vecmath.Vector3d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.entity.Entity;

import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.client.render.IconUtil;
import com.enderio.core.client.render.RenderUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RangeRenderer extends RenderEntity {

    @Override
    public void doRender(Entity entity, double x, double y, double z, float p_76986_8_, float p_76986_9_) {

        RangeEntity se = ((RangeEntity) entity);
        AxisAlignedBB rangeBB = se.getBoundingBox();
        final Tessellator tessellator = Tessellator.instance;


        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);

        Vector3d translate = new Vector3d(
            -(se.posX - rangeBB.minX + 0.01),
            -(se.posY - rangeBB.minY + 0.01),
            -(se.posZ - rangeBB.minZ + 0.01)
        );

        Vector3d scale = new Vector3d(
            1 - (se.lifeSpan / (float) se.totalLife),
            1 - (se.lifeSpan / (float) se.totalLife),
            rangeBB.maxZ - rangeBB.minZ
        );

        scale.x = Math.min(scale.x, 1);
        scale.y = Math.min(scale.y, 1);

        scale.x *= (rangeBB.maxX - rangeBB.minX);
        scale.y *= (rangeBB.maxY - rangeBB.minY);

        GL11.glPushMatrix();
        GL11.glColor4f(se.getColor()[0], se.getColor()[1], se.getColor()[2], se.getColor()[3]);
        RenderUtil.bindBlockTexture();
        tessellator.startDrawingQuads();
        GL11.glTranslated(x, y, z);
        GL11.glTranslated(translate.x, translate.y, translate.z);
        GL11.glScaled(scale.x, scale.y, scale.z);
        tessellator.setBrightness(15 << 20 | 15 << 4);
        CubeRenderer.get().render(BoundingBox.UNIT_CUBE, IconUtil.whiteTexture);
        tessellator.draw();

        RenderUtil.bindItemTexture();

        GL11.glDepthMask(true);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }
}

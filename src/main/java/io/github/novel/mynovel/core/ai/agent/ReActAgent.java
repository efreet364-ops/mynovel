package io.github.novel.mynovel.core.ai.agent;

import lombok.Data;

@Data
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     * @return 是否需要执行行动
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     * @return 行动执行的结果
     */
    public abstract String act();


    /**
     * 执行单个步骤：思考和行动
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "步骤执行失败: " + e.getMessage();
        }
    }
}

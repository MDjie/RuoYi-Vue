# -*- coding: utf-8 -*-
import json
import sys
from sklearn.metrics import precision_score, recall_score, f1_score
from sklearn.preprocessing import LabelEncoder

def evaluate_answers(reference_path, test_path):
    """
    比较参考答案文件和试卷文件，计算准确率和F1分数
    
    参数:
        reference_path (str): 参考答案文件路径
        test_path (str): 试卷文件路径
    """
    try:
        # 读取参考答案文件，建立text到answer的映射
        reference_map = {}
        with open(reference_path, 'r', encoding='utf-8') as f:
            for line in f:
                try:
                    data = json.loads(line.strip())
                    if 'text' in data and 'Human_Answer' in data:
                        # 统一转换为字符串类型
                        reference_map[data['text']] = str(data['Human_Answer'])
                except json.JSONDecodeError:
                    continue
        
        # 读取试卷文件，收集所有非空Human_Answer
        true_labels = []
        pred_labels = []
        total_questions = 0
        matched_questions = 0
        
        with open(test_path, 'r', encoding='utf-8') as f:
            for line in f:
                try:
                    data = json.loads(line.strip())
                    if 'text' in data and 'Human_Answer' in data and data['Human_Answer']:
                        total_questions += 1
                        text = data['text']
                        # 统一转换为字符串类型
                        human_answer = str(data['Human_Answer'])
                        
                        if text in reference_map:
                            matched_questions += 1
                            true_labels.append(reference_map[text])
                            pred_labels.append(human_answer)
                        else:
                            print(f"未找到匹配的问题: {text}")
                except json.JSONDecodeError:
                    continue
        
        # 计算指标
        if matched_questions == 0:
            print("没有找到匹配的问题")
            return
        
        # 使用LabelEncoder将标签统一编码
        le = LabelEncoder()
        all_labels = true_labels + pred_labels
        le.fit(all_labels)
        
        true_encoded = le.transform(true_labels)
        pred_encoded = le.transform(pred_labels)
        
        accuracy = sum(1 for t, p in zip(true_encoded, pred_encoded) if t == p) / matched_questions
        precision = precision_score(true_encoded, pred_encoded, average='weighted', zero_division=0)
        recall = recall_score(true_encoded, pred_encoded, average='weighted', zero_division=0)
        f1 = f1_score(true_encoded, pred_encoded, average='weighted', zero_division=0)
            
        # 输出结果
        result = f"\n在文件{reference_path}的评估结果:\n"
        result += f"总问题数: {total_questions}\n"
        result += f"匹配到参考答案的问题数: {matched_questions}\n"
        result += f"准确率(Accuracy): {accuracy:.4f}\n"
        result += f"精确率(Precision): {precision:.4f}\n"
        result += f"召回率(Recall): {recall:.4f}\n"
        result += f"F1分数: {f1:.4f}\n"
        
        print(result)
        return result
    
    except FileNotFoundError as e:
        error_msg = f"文件未找到: {str(e)}"
        print(error_msg)
        return error_msg
    except Exception as e:
        error_msg = f"发生错误: {str(e)}"
        print(error_msg)
        return error_msg

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python Check_Score.py <reference_file> <test_file>")
        sys.exit(1)
    
    reference_file = sys.argv[1]
    test_file = sys.argv[2]
    evaluate_answers(reference_file, test_file)
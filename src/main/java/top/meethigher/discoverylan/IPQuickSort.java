package top.meethigher.discoverylan;

/**
 * IP地址快速排序
 *
 * @author chenchuancheng
 * @since 2023/4/4 8:58
 */
public class IPQuickSort {


    public static void quickSort(String[] arr, int left, int right) {
        if (left < right) {
            int pivotIndex = partition(arr, left, right);
            quickSort(arr, left, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, right);
        }
    }

    private static int partition(String[] arr, int left, int right) {
        String pivot = arr[right];
        int i = left - 1;
        for (int j = left; j < right; j++) {
            if (lt(arr[j], pivot)) {
                i++;
                String temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        String temp = arr[i + 1];
        arr[i + 1] = arr[right];
        arr[right] = temp;
        return i + 1;
    }


    private static boolean lt(String font, String back) {
        String[] fontArr = font.replaceAll("\\[|\\]", "").split("---")[0].trim().split("\\.");
        String[] backArr = back.replaceAll("\\[|\\]", "").split("---")[0].trim().split("\\.");

        for (int i = 0; i < 4; i++) {
            int a = Integer.parseInt(fontArr[i]);
            int b = Integer.parseInt(backArr[i]);
            if (a < b) {
                return true;
            }
        }
        return false;
    }


}

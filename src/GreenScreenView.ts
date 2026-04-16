import { requireNativeComponent, ViewProps } from 'react-native';

export interface GreenScreenVideoViewNativeProps extends ViewProps {
    mirror?: boolean;
    objectFit?: 'contain' | 'cover';
    streamURL?: string;
    zOrder?: number;
    onDimensionsChange?: (event: { nativeEvent: { width: number; height: number } }) => void;
}

export default requireNativeComponent<GreenScreenVideoViewNativeProps>('GreenScreenVideoView');

/**
 * @format
 */

import React from 'react';
import ReactTestRenderer from 'react-test-renderer';
import App from '../App';

jest.mock('react-native-webrtc', () => ({
  mediaDevices: { getUserMedia: jest.fn() },
  startIOSPIP: jest.fn(),
  stopIOSPIP: jest.fn(),
  RTCPIPView: 'RTCPIPView',
}));

test('renders correctly', async () => {
  await ReactTestRenderer.act(() => {
    ReactTestRenderer.create(<App />);
  });
});
